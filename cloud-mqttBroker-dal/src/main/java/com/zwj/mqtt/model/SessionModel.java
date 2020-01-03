package com.zwj.mqtt.model;

import com.zwj.mqtt.serialize.GsonSerialize;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zwj
 * @Date: 2019-11-19 15:49
 */
@Data
@AllArgsConstructor
public class SessionModel {

    private Integer brokerId;

    private String clientId;

    private Boolean cleanSession;

    private Boolean hasWill;

    private String payload;

    private Integer qosLevel;

    private String topicName;

    @Override
    public String toString() {
        return GsonSerialize.getGSON().toJson(this);
    }

}
