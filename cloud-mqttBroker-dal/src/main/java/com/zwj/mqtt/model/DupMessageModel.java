package com.zwj.mqtt.model;

import com.zwj.mqtt.serialize.GsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zwj
 * @Date: 2019-11-22 13:57
 */
@Data
@AllArgsConstructor
public class DupMessageModel {

    private String clientId;

    private String topic;

    private int mqttQoS;

    private int messageId;

    private String payload;

    @Override
    public String toString() {
        return GsonSerialize.getGSON().toJson(this);
    }
}
