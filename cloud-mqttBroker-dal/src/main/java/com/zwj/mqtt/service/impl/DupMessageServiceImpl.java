package com.zwj.mqtt.service.impl;

import com.zwj.mqtt.model.DupMessageModel;
import com.zwj.mqtt.serialize.GsonSerialize;
import com.zwj.mqtt.service.DupMessageService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-22 14:04
 */
@Component
@Slf4j
public class DupMessageServiceImpl implements DupMessageService {

    private static final String DUP_MESSAGE_PRE = "dupMessage:";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void putDupMessage(int messageId, String clientId, DupMessageModel dupMessageModel) {
        stringRedisTemplate.opsForHash().put(DUP_MESSAGE_PRE + clientId, messageId, dupMessageModel.toString());
    }

    @Override
    public DupMessageModel getDupMessageByMessageId(int messageId, String clientId) {
        String dupMessageModelJson = (String) stringRedisTemplate.opsForHash().get(DUP_MESSAGE_PRE + clientId, messageId);
        return GsonSerialize.getGSON().fromJson(dupMessageModelJson, DupMessageModel.class);
    }

    @Override
    public void deleteDupMessageByClientId(String clientId) {
        stringRedisTemplate.delete(DUP_MESSAGE_PRE + clientId);
    }

    @Override
    public void deleteDupMessageByMessageIdAndClientId(int messageId, String clientId) {
        stringRedisTemplate.opsForHash().delete(DUP_MESSAGE_PRE + clientId, messageId);
    }

    @Override
    public Map<Object, Object> getDupMessageModelByClientId(String clientId) {
        return stringRedisTemplate.opsForHash().entries(DUP_MESSAGE_PRE + clientId);
    }
}
