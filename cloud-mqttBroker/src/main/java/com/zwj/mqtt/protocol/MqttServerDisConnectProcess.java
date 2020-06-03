package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.DISCONNECT;

import com.zwj.mqtt.model.SessionModel;
import com.zwj.mqtt.service.DupMessageService;
import com.zwj.mqtt.service.MessageIdService;
import com.zwj.mqtt.service.SessionService;
import com.zwj.mqtt.service.SubScribeService;
import com.zwj.mqtt.constants.ServerConstant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 17:45
 */
@Component
@Slf4j
public class MqttServerDisConnectProcess extends AbstractMqttProtocol {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private SubScribeService subScribeService;

    @Autowired
    private DupMessageService dupMessageService;

    @Autowired
    private MessageIdService messageIdService;

    @Override
    public int getMessageType() {
        return DISCONNECT.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        disConnect(channelHandlerContext.channel(), mqttMessage);
    }

    @Override
    public void disConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        channel.attr(AttributeKey.valueOf("clientId")).set(null);
        log.debug("DISCONNECT - clientId: {}", clientId);
        ServerConstant.CHANNEL_MAP.remove(clientId);
        // 正常下线删除遗嘱消息
        SessionModel sessionModel = sessionService.getSessionByClientId(clientId);
        if (sessionModel != null) {
            if (sessionModel.getCleanSession()) {
                subScribeService.delSubScribe(clientId);
                dupMessageService.deleteDupMessageByClientId(clientId);
                messageIdService.delMessageId(clientId);
                sessionService.removeByClientId(clientId);
            }
        }
        channel.close();
    }
}
