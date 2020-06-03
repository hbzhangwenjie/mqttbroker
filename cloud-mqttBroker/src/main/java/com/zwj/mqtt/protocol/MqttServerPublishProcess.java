package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.PUBLISH;

import com.zwj.mqtt.cluster.MessageDto;
import com.zwj.mqtt.cluster.Producer;
import com.zwj.mqtt.config.BrokerProperties;
import com.zwj.mqtt.model.DupMessageModel;
import com.zwj.mqtt.model.RetainMessageModel;
import com.zwj.mqtt.model.SessionModel;
import com.zwj.mqtt.security.IdGenerator;
import com.zwj.mqtt.service.DupMessageService;
import com.zwj.mqtt.service.MessageIdService;
import com.zwj.mqtt.service.RetainMessageService;
import com.zwj.mqtt.service.SessionService;
import com.zwj.mqtt.service.SubScribeService;
import com.zwj.mqtt.constants.ServerConstant;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-19 17:55
 */
@Component
@Slf4j
public class MqttServerPublishProcess extends AbstractMqttProtocol {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private BrokerProperties brokerProperties;

    @Autowired
    private SubScribeService subScribeService;

    @Autowired
    private DupMessageService dupMessageService;

    @Autowired
    private MessageIdService messageIdService;

    @Autowired
    private RetainMessageService retainMessageService;

    @Autowired
    private Producer producer;

    @Override
    public int getMessageType() {
        return PUBLISH.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        publish(channelHandlerContext.channel(), (MqttPublishMessage) mqttMessage);
    }

    @Override
    public void publish(Channel channel, MqttPublishMessage msg) {
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().readBytes(messageBytes);
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        switch (msg.fixedHeader().qosLevel()) {
            case AT_MOST_ONCE:
                messageReactor(clientId, msg, messageBytes);
                //qos = 0 ,不用返回
                break;
            case AT_LEAST_ONCE:
                //qos = 1 ,返回应答ask 带报文标示符
                MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(msg.variableHeader().packetId()), null);
                channel.writeAndFlush(pubAckMessage);
                messageReactor(clientId, msg, messageBytes);
                break;
            case EXACTLY_ONCE:
                // qos = 2 ,返回应答
                MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.EXACTLY_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(msg.variableHeader().packetId()), null);
                channel.writeAndFlush(pubRecMessage);
                // 缓存消息标示符
                if (!(msg.fixedHeader().isDup() && messageIdService.containMessageId(clientId, msg.variableHeader().packetId()))) {
                    messageReactor(clientId, msg, messageBytes);
                    messageIdService.putMessageId(clientId, msg.variableHeader().packetId());
                }
                break;
            default:
                log.warn("MqttPublishMessage 没有 qosLevel ,msg{}", msg);
                return;
        }
        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            // 存储保留消息
            retainMessageService.putRetainMessage(msg.variableHeader().topicName(),
                    new RetainMessageModel(msg.fixedHeader().qosLevel().value(), Base64.getEncoder().encodeToString(messageBytes)));
        }
    }

    private void messageReactor(String clientId, MqttPublishMessage msg, byte[] messageBytes) {
        SessionModel sessionModel = sessionService.getSessionByClientId(clientId);
        log.debug("Publish - clientId: {}, cleanSession: {}", clientId, msg);
        if (brokerProperties.getAutoSub()) {
            // 异步通过tcp/dubbo/http？ todo 将消息发给hubService

        } else {
            // 参照mqtt的协议把消息发往订阅者。
            sendMessageToSub(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, msg.fixedHeader().isRetain(),
                    msg.fixedHeader().isDup());
        }
    }

    public void sendMessageToSub(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        Set<String> subScribes = subScribeService.getSubScribes(topic);
        subScribes.forEach(subScribe -> {
            String[] subPara = subScribe.split("-");
            String clientId = subPara[0];
            int subQos = Integer.parseInt(subPara[1]);
            int qos = Math.min(mqttQoS.value(), subQos);
            //订阅者连接还在
            Channel channel;
            if ((channel = ServerConstant.CHANNEL_MAP.get(clientId)) != null) {
                doPublish(clientId, MqttQoS.valueOf(qos), channel, topic, messageBytes, retain, dup);
            } else {
                //发给其他broker
                MessageDto messageDto = new MessageDto(clientId, retain, dup, topic, qos,
                        Base64.getEncoder().encodeToString(messageBytes));
                producer.send(brokerProperties.getMessageSyncTopic(), messageDto.toString());
            }
        });

    }

    public void doPublish(String clientId, MqttQoS pubQoS, Channel channel, String topic, byte[] messageBytes, boolean retain, boolean dup) {
        //消息质量是发布者和订阅者中最大的
        switch (pubQoS) {
            case AT_MOST_ONCE:
                //直接发
                MqttPublishMessage mostMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, dup, pubQoS, retain, 0),
                        new MqttPublishVariableHeader(topic, 0), PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(messageBytes));
                channel.writeAndFlush(mostMessage);
                break;
            case AT_LEAST_ONCE:
                //先存消息在发
                int leastMessageId = IdGenerator.getMessageId(brokerProperties.getBrokerId());
                MqttPublishMessage leastMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, dup, pubQoS, retain, 0),
                        new MqttPublishVariableHeader(topic, leastMessageId), PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(messageBytes));
                dupMessageService.putDupMessage(leastMessageId, clientId,
                        new DupMessageModel(clientId, topic, pubQoS.value(), leastMessageId,
                                Base64.getEncoder().encodeToString(messageBytes)));
                channel.writeAndFlush(leastMessage);
                break;
            case EXACTLY_ONCE:
                //先存消息在发
                int exactlyMessageId = IdGenerator.getMessageId(brokerProperties.getBrokerId());
                MqttPublishMessage exactlyMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, dup, pubQoS, retain, 0),
                        new MqttPublishVariableHeader(topic, exactlyMessageId), PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(messageBytes));
                dupMessageService.putDupMessage(exactlyMessageId, clientId,
                        new DupMessageModel(clientId, topic, pubQoS.value(), exactlyMessageId,
                                Base64.getEncoder().encodeToString(messageBytes)));
                channel.writeAndFlush(exactlyMessage);
                break;
            default:
                break;

        }
        log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", clientId, topic, pubQoS.value());
    }
}
