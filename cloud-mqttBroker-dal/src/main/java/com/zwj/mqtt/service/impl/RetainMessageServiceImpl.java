package com.zwj.mqtt.service.impl;

import static com.zwj.mqtt.serialize.GsonSerialize.getGSON;

import com.zwj.mqtt.model.RetainMessageModel;
import com.zwj.mqtt.service.RetainMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-12-03 16:06
 */
@Component
@Slf4j
public class RetainMessageServiceImpl implements RetainMessageService {

    private static final String RETAIN_PRE = "retain:";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void putRetainMessage(String topic, RetainMessageModel retainMessageModel) {
        if (retainMessageModel == null) {
            stringRedisTemplate.delete(RETAIN_PRE + topic);
        } else {
            stringRedisTemplate.opsForValue().set(RETAIN_PRE + topic, retainMessageModel.toString());
        }
    }

    @Override
    public RetainMessageModel getRetainMessage(String topic) {
        String dupMessageModelJson = stringRedisTemplate.opsForValue().get(RETAIN_PRE + topic);
        if (dupMessageModelJson != null) {
            return getGSON().fromJson(dupMessageModelJson, RetainMessageModel.class);
        } else {
            return null;
        }

    }
}
