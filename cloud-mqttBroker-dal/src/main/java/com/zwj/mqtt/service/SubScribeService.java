package com.zwj.mqtt.service;

import java.util.Collection;
import java.util.Set;

/**
 * @Author: zwj
 * @Date: 2019-11-21 17:23
 */
public interface SubScribeService {

    void putSubScribe( String topic, String clientId, int qos);

    Set<String> getSubScribes( String topic);

    boolean containSubScribe(String topic, String clientId, int qos);

    void delSubScribe(String clientId);

    void delSubScribeByTopic( Collection<String> topics, String clientId);
}
