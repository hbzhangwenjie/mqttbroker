package com.zwj.mqtt.service.impl;

import com.zwj.mqtt.service.SubScribeService;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-22 09:37
 */
@Slf4j
@Component
public class SubscribeServiceImpl implements SubScribeService {

    private static final String SUBSCRIBE_PRE = "subscribe:";
    private static final String PRE = "topics:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void putSubScribe(String topic, String clientId, int qos) {
        stringRedisTemplate.opsForSet().add(SUBSCRIBE_PRE + PRE, topic);
        stringRedisTemplate.opsForSet().add(SUBSCRIBE_PRE  + ":" + topic, clientId + "-" + qos);
    }

    @Override
    public Set<String> getSubScribes( String topic) {
        return stringRedisTemplate.opsForSet().members(SUBSCRIBE_PRE  + ":" + topic);
    }

    @Override
    public boolean containSubScribe(String topic, String clientId, int qos) {
        return stringRedisTemplate.opsForSet().isMember(SUBSCRIBE_PRE  + ":" + topic, clientId + "-" + qos);
    }

    @Override
    public void delSubScribe(String clientId) {
        Set<String> topics = stringRedisTemplate.opsForSet().members(SUBSCRIBE_PRE + PRE);
        delSubScribeByTopic(topics,clientId);
    }

    @Override
    public void delSubScribeByTopic( Collection<String> topics, String clientId) {
        if (topics != null) {
            for (String topic : topics) {
                stringRedisTemplate.opsForSet()
                        .remove(SUBSCRIBE_PRE  + ":" + topic, clientId + "-" + 0, clientId + "-" + 1, clientId + "-" + 2);
            }
        }
    }
}
