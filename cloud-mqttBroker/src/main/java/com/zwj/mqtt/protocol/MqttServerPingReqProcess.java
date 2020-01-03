package com.zwj.mqtt.protocol;

import static io.netty.handler.codec.mqtt.MqttMessageType.PINGREQ;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-20 17:38
 */
@Slf4j
@Component
public class MqttServerPingReqProcess extends AbstractMqttProtocol {

    @Override
    public int getMessageType() {
        return PINGREQ.value();
    }

    @Override
    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        pingReq(channelHandlerContext.channel(),mqttMessage);
    }

    @Override
    public void pingReq(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
            log.debug("PINGREQ - clientId: {}", clientId);
            channel.writeAndFlush(pingRespMessage);
    }
}
