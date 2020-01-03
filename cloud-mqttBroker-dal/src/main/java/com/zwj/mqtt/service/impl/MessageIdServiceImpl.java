package com.zwj.mqtt.service.impl;

import com.zwj.mqtt.service.MessageIdService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-22 14:51
 */
@Component
@Slf4j
public class MessageIdServiceImpl implements MessageIdService {

    private static final String MESSAGE_ID = "messageId:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void putMessageId(String clientId, int messageId) {
        stringRedisTemplate.opsForSet().add(MESSAGE_ID + clientId, String.valueOf(messageId));
    }

    @Override
    public void delMessageIdByMessageId(String clientId, int messageId) {
        stringRedisTemplate.opsForSet().remove(MESSAGE_ID + clientId, String.valueOf(messageId));
    }

    @Override
    public boolean containMessageId(String clientId, int messageId) {
        return stringRedisTemplate.opsForSet().isMember(MESSAGE_ID + clientId, String.valueOf(messageId));
    }

    @Override
    public void delMessageId(String clientId) {
        stringRedisTemplate.delete(MESSAGE_ID + clientId);
    }

    @Override
    public Set<String> getMessageId(String clientId) {
        return stringRedisTemplate.opsForSet().members(MESSAGE_ID + clientId);
    }
}
