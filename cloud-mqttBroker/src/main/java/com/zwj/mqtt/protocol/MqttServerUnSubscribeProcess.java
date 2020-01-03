package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.UNSUBSCRIBE;

import com.zwj.mqtt.service.SubScribeService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 17:24
 */
@Component
@Slf4j
public class MqttServerUnSubscribeProcess extends AbstractMqttProtocol {

    @Autowired
    private SubScribeService subScribeService;

    @Override
    public int getMessageType() {
        return UNSUBSCRIBE.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        unSubscribe(channelHandlerContext.channel(), (MqttUnsubscribeMessage) mqttMessage);
    }

    @Override
    public void unSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        subScribeService.delSubScribeByTopic(msg.payload().topics(), clientId);
        MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        channel.writeAndFlush(unsubAckMessage);
    }

}
