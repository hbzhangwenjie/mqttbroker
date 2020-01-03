package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.PUBCOMP;

import com.zwj.mqtt.service.MessageIdService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 16:37
 */
@Component
@Slf4j
public class MqttServerPubCompProcess extends AbstractMqttProtocol {

    @Autowired
    private MessageIdService messageIdService;

    @Override
    public int getMessageType() {
        return PUBCOMP.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        pubComp(channelHandlerContext.channel(), (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
    }

    @Override
    public void pubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        //删除报文标示符号
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        messageIdService.delMessageIdByMessageId(clientId, variableHeader.messageId());
        log.debug("PUBCOMP - clientId: {}, messageId: {}", clientId, variableHeader.messageId());


    }
}
