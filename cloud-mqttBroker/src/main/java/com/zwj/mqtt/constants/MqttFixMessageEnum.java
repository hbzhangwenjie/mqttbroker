package com.zwj.mqtt.constants;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @Author: Jimmy
 * @Date: 2020-01-14 15:23
 */
public enum MqttFixMessageEnum {
    PINGESRP(MqttMessageFactory.newMessage(
            new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null));
    private final MqttMessage mqttMessage;

    MqttFixMessageEnum(MqttMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public MqttMessage getMqttMessage() {
        return mqttMessage;
    }
}
