package com.zwj.mqtt.model;

import com.zwj.mqtt.serialize.GsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zwj
 * @Date: 2019-12-03 14:56
 */
@Data
@AllArgsConstructor
public class RetainMessageModel {

    private int mqttQoS;

    private String payload;

    @Override
    public String toString() {
        return GsonSerialize.getGSON().toJson(this);
    }
}
