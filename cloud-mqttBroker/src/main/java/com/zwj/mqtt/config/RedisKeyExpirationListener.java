package com.zwj.mqtt.config;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-12-18 14:23
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    /**
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     * notify-keyspace-events Ex redis 服务该配置打开
     * copy form https://www.cnblogs.com/sunsing123/p/10304184.html
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        //消息质量为1/2的消息到期重试
    }
}
