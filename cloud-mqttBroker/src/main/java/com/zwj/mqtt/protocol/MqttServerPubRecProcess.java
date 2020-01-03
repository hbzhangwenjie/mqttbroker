package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.PUBREC;

import com.zwj.mqtt.service.DupMessageService;
import com.zwj.mqtt.service.MessageIdService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 15:39
 */
@Component
@Slf4j
public class MqttServerPubRecProcess extends AbstractMqttProtocol {

    @Autowired
    private DupMessageService dupMessageService;
    @Autowired
    private MessageIdService messageIdService;

    @Override
    public int getMessageType() {
        return PUBREC.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        pubRec(channelHandlerContext.channel(), (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
    }

    @Override
    public void pubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
        log.debug("PUBREC - clientId: {}, messageId: {}", clientId, variableHeader.messageId());
        // 删除pubMessage 存储rec报文标示符
        dupMessageService.deleteDupMessageByMessageIdAndClientId(variableHeader.messageId(), clientId);
        messageIdService.putMessageId(clientId, variableHeader.messageId());
        channel.writeAndFlush(pubRelMessage);
    }
}
