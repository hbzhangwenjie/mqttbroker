package com.zwj.mqtt.cluster;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @Author: zwj
 * @Date: 2019-12-02 15:50
 */
@Component
@Slf4j
public class Producer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("kafka send topic [{}]  message [{}] error", topic, message, ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                log.debug("kafka topic [{}]  message [{}]  send, result{}", topic,
                        message, result.getProducerRecord());
            }
        });
    }
}
