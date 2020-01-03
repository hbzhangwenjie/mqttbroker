package com.zwj.mqtt.service.impl;

import static com.zwj.mqtt.serialize.GsonSerialize.getGSON;

import com.zwj.mqtt.model.SessionModel;
import com.zwj.mqtt.serialize.GsonSerialize;
import com.zwj.mqtt.service.SessionService;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-11-21 13:26
 */
@Component
@Slf4j
public class SessionServiceImpl implements SessionService {

    private static final String SESSION_PRE = "session:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean containClientId(String clientId) {
        return stringRedisTemplate.hasKey(SESSION_PRE + clientId);
    }

    @Override
    public SessionModel getSessionByClientId(String clientId) {
        String sessionModelJson = stringRedisTemplate.opsForValue().get(SESSION_PRE + clientId);
        log.debug("getSessionByClientId,clientId:{},result:{}", clientId, sessionModelJson);
        return getGSON().fromJson(sessionModelJson, SessionModel.class);
    }

    @Override
    public void removeByClientId(String clientId) {
        stringRedisTemplate.delete(SESSION_PRE + clientId);
    }

    @Override
    public void putSessionByClient(String clientId, SessionModel sessionModel) {
        log.debug("putSessionByClient,clientId:{},sessionModel:{}", clientId, sessionModel.toString());
        stringRedisTemplate.opsForValue().set(SESSION_PRE + clientId, sessionModel.toString());
    }

}
