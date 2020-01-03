package com.zwj.mqtt.protocol;

import static com.zwj.mqtt.constants.ServerConstant.CHANNEL_MAP;
import static io.netty.handler.codec.mqtt.MqttMessageType.CONNECT;

import com.zwj.mqtt.cluster.Producer;
import com.zwj.mqtt.config.BrokerProperties;
import com.zwj.mqtt.model.DupMessageModel;
import com.zwj.mqtt.model.SessionModel;
import com.zwj.mqtt.serialize.GsonSerialize;
import com.zwj.mqtt.service.DupMessageService;
import com.zwj.mqtt.service.MessageIdService;
import com.zwj.mqtt.service.SessionService;
import com.zwj.mqtt.service.SubScribeService;
import com.zwj.mqtt.constants.ServerConstant;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author: zwj
 * @Date: 2019-11-18 19:31
 */
@Component
@Slf4j
public class MqttServerConnectProcess extends AbstractMqttProtocol {

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
    private Producer producer;

    @Override
    public int getMessageType() {
        return CONNECT.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        connect(channelHandlerContext.channel(), (MqttConnectMessage) mqttMessage);
    }

    /**
     * 连接服务端：客户端发送给服务端的第一个报文必须是CONNECT报文
     */
    @Override
    public void connect(Channel channel, MqttConnectMessage msg) {

        // 1.clientId为空或null的情况, 这里要求客户端必须提供clientId
        if (StringUtils.isEmpty(msg.payload().clientIdentifier())) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        }
        //2.是否需要验证用户名密码
        if (msg.variableHeader().hasPassword()) {
            //验证密码
        }
        //3.获取clientId
        String clientId = msg.payload().clientIdentifier();
        Channel channelOld = CHANNEL_MAP.get(clientId);
        if (channelOld != null) {
            //关闭之前的连接
            channelOld.close();
        } else {
            //防止一个clientId 登陆2个broker
            producer.send(brokerProperties.getMessageSyncTopic(), brokerProperties.getBrokerId()+"_"+clientId);
        }

        //4.设置保活时间 客户端发ping的间隔,0业务不保活 标示客户端不会断开，tcp心跳so_keepAlive
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            int expire = Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f);
            channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, expire));
        }
        boolean sessionPresent = false;
        SessionModel sessionModel = new SessionModel(brokerProperties.getBrokerId(), msg.payload().clientIdentifier(),
                msg.variableHeader().isCleanSession(), null, null, null, null);
        if (msg.variableHeader().isCleanSession()) {
            //删除之前的会话重新建立会话状态
            sessionService.removeByClientId(clientId);
            // 删除订阅 和 dup
            subScribeService.delSubScribe(clientId);
            dupMessageService.deleteDupMessageByClientId(clientId);
            messageIdService.delMessageId(clientId);
        } else {
            SessionModel oldSessionModel;
            if ((oldSessionModel = sessionService.getSessionByClientId(clientId)) != null) {
                sessionPresent = !oldSessionModel.getCleanSession();
                if (oldSessionModel.getHasWill() && !msg.variableHeader().isWillFlag()) {
                    sessionModel.setPayload(oldSessionModel.getPayload());
                    sessionModel.setHasWill(Boolean.TRUE);
                    sessionModel.setQosLevel(oldSessionModel.getQosLevel());
                    sessionModel.setTopicName(oldSessionModel.getTopicName());
                }
            }
        }

        //5.有遗嘱消息
        if (msg.variableHeader().isWillFlag()) {
            sessionModel.setPayload(Base64.getEncoder().encodeToString(msg.payload().willMessageInBytes()));
            sessionModel.setHasWill(Boolean.TRUE);
            sessionModel.setQosLevel(msg.variableHeader().willQos());
            sessionModel.setTopicName(msg.payload().willTopic());
        }
        //6.存储会话
        //有则更新没有则新建了
        sessionService.putSessionByClient(clientId, sessionModel);
        channel.attr(AttributeKey.valueOf("clientId")).set(clientId);
        CHANNEL_MAP.put(clientId, channel);
        /**
         * 会话存在标识（Session Present Flag）：用于标识在 Broker 上是否已存在该 Client（用 Client Identifier 区分）的持久性会话，1bit，0 或者 1。
         * 当Client在连接时设置Clean Session=1（会话清除标识见CONNECT数据包的可变头），则CONNACK中的Session Present Flag始终为0；
         * 当 Client 在连接时设置 Clean Session=0，那么存在下面两种情况
         * 如果Broker上面保留了这个Client之前留下的持久性会话，那么CONNACK中的Session Present Flag值为1；
         * 如果Broker上面没有保存这个Client之前留下的会话数据，那么CONNACK中的Session Present Flag值为0；
         */
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        channel.writeAndFlush(okResp);

        log.debug("connect - clientId: {}, connectMsg: {},connectAckMsg:{}", clientId, msg, okResp);
        // 8.如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!msg.variableHeader().isCleanSession()) {
            Map<Object, Object> dups = dupMessageService.getDupMessageModelByClientId(clientId);
            for (Object messageId : dups.keySet()) {
                String dupMessageModel = (String) dups.get(messageId);
                DupMessageModel dup = GsonSerialize.getGSON().fromJson(dupMessageModel, DupMessageModel.class);
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dup.getMqttQoS()), false, 0),
                        new MqttPublishVariableHeader(dup.getTopic(), dup.getMessageId()),
                        PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(dup.getPayload().getBytes(
                                StandardCharsets.UTF_8)));
                channel.writeAndFlush(publishMessage);
            }
            Set<String> messageIds = messageIdService.getMessageId(clientId);
            messageIds.forEach(messageId -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(Integer.parseInt(messageId)), null);
                channel.writeAndFlush(pubRelMessage);
            });
        }
    }
}
