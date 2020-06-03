package com.zwj.mqtt.server;

import static com.zwj.mqtt.constants.ServerConstant.CHANNEL_MAP;

import com.zwj.mqtt.config.MqttServerProtocolProcessor;
import com.zwj.mqtt.model.SessionModel;
import com.zwj.mqtt.protocol.MqttServerPublishProcess;
import com.zwj.mqtt.service.SessionService;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-18 13:55
 */
@Component
@Sharable
@Slf4j
public class MqttServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

    @Autowired
    private MqttServerProtocolProcessor mqttServerProtocolProcessor;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private MqttServerPublishProcess mqttServerPublishProcess;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sendWill(ctx);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        //1.消息解码结果
        if (mqttMessage.decoderResult().isFailure()) {
            //解码异常
            Throwable cause = mqttMessage.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                //不支持的协议版本
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                channelHandlerContext.writeAndFlush(connAckMessage);
            } else if (cause instanceof MqttIdentifierRejectedException) {
                //不合格的clientId
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                channelHandlerContext.writeAndFlush(connAckMessage);
            }
            channelHandlerContext.close();
            return;
        }
        mqttServerProtocolProcessor.process(channelHandlerContext, mqttMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("mqtt broker has a cause:{}", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //约定的周期没有ping 过来，断开连接发布遗嘱
            sendWill(ctx);
        }
        super.userEventTriggered(ctx, evt);
    }

    private void sendWill(ChannelHandlerContext ctx) {
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("clientId")).get();
        log.debug("inactiveHandler,channelId:{},clientId:{}",ctx.channel().id(),clientId);
        if (!StringUtil.isNullOrEmpty(clientId)) {
            SessionModel sessionModel;
            // 发送遗嘱消息
            if ((sessionModel = sessionService.getSessionByClientId(clientId)) != null) {
                if (sessionModel.getHasWill()) {
                    mqttServerPublishProcess
                            .sendMessageToSub(sessionModel.getTopicName(), MqttQoS.valueOf(sessionModel.getQosLevel()),
                                    sessionModel.getPayload().getBytes(StandardCharsets.UTF_8), false,
                                    false);
                }
            }
            // 连接不活跃了
            ctx.channel().attr(AttributeKey.valueOf("clientId")).set(null);
            CHANNEL_MAP.remove(clientId);
        }
    }
}
