package com.zwj.mqtt.cluster;

import com.zwj.mqtt.serialize.GsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zwj
 * @Date: 2019-12-02 16:18
 */
@Data
@AllArgsConstructor
public class MessageDto {

    private String clientId;

    private boolean retain;

    private boolean dup;

    private String topic;

    private int mqttQoS;

    private String payload;

    @Override
    public String toString() {
        return GsonSerialize.getGSON().toJson(this);
    }
}
