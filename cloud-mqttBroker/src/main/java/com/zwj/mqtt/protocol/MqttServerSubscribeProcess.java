package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.SUBSCRIBE;

import com.zwj.mqtt.model.RetainMessageModel;
import com.zwj.mqtt.service.RetainMessageService;
import com.zwj.mqtt.service.SubScribeService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.util.AttributeKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 17:20
 */
@Component
@Slf4j
public class MqttServerSubscribeProcess extends AbstractMqttProtocol {

    @Autowired
    private SubScribeService subScribeService;
    @Autowired
    private RetainMessageService retainMessageService;
    @Autowired
    private MqttServerPublishProcess mqttServerPublishProcess;

    @Override
    public int getMessageType() {
        return SUBSCRIBE.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        subscribe(channelHandlerContext.channel(), (MqttSubscribeMessage) mqttMessage);
    }

    @Override
    public void subscribe(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<Integer> mqttQoSList = new ArrayList<>();
        log.debug("subscribe - clientId: {},msg:{}", clientId, msg);
        MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                new MqttSubAckPayload(mqttQoSList));
        channel.writeAndFlush(subAckMessage);

        topicSubscriptions.forEach(topicSubscription -> {
            String topicName = topicSubscription.topicName();
            MqttQoS mqttQoS = topicSubscription.qualityOfService();
            subScribeService.putSubScribe(topicName, clientId, mqttQoS.value());
            mqttQoSList.add(mqttQoS.value());
            log.debug("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicName, mqttQoS.value());
            RetainMessageModel retainMessageModel = retainMessageService.getRetainMessage(topicName);
            if(retainMessageModel!=null){
                int qos = Math.min(mqttQoS.value(), retainMessageModel.getMqttQoS());
                mqttServerPublishProcess
                        .doPublish(clientId, MqttQoS.valueOf(qos), channel, topicName, Base64.getDecoder().decode(retainMessageModel.getPayload().getBytes(
                                StandardCharsets.UTF_8)), false,
                                false);
            }
        });
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // todo 校验topic名字
        }
        return true;
    }
}
