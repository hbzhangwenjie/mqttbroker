package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.PUBACK;

import com.zwj.mqtt.service.DupMessageService;
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
 * @Date: 2019-11-20 15:29
 */
@Component
@Slf4j
public class MqttServerPubAckProcess extends AbstractMqttProtocol {

    @Autowired
    private DupMessageService dupMessageService;

    @Override
    public int getMessageType() {
        return PUBACK.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        pubAck(channelHandlerContext.channel(), (MqttMessageIdVariableHeader) mqttMessage.variableHeader());
    }

    @Override
    public void pubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        log.debug("PUBACK - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        //删除缓存的消息 qos=1
        dupMessageService.deleteDupMessageByMessageIdAndClientId(messageId,clientId);
    }

}
