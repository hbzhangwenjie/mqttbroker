package com.zwj.mqtt.config;

import com.zwj.mqtt.protocol.MqttProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author: zwj
 * @Date: 2019-11-19 17:35
 */
@Component
@Slf4j
public class MqttServerProtocolProcessor implements ApplicationListener<ApplicationReadyEvent> {

    private static final HashMap<Integer, MqttProtocol> PROCESSORS = new HashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Map<String, MqttProtocol> processors = event.getApplicationContext()
                .getBeansOfType(MqttProtocol.class);
        processors.forEach((key, processor) -> PROCESSORS.putIfAbsent(processor.getMessageType(), processor));
    }

    public void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) {
        //除了connect 类型的消息都要校验clientId 是否存在
        if (mqttMessage.fixedHeader().messageType().value() != 1 && StringUtils
                .isEmpty(channelHandlerContext.channel().attr(AttributeKey.valueOf("clientId")).get())) {
            //不能绕过connect消息 直接发其他类型的消息
            log.warn("发msg:{}，之前还没有发connect消息", mqttMessage);
            return;
        }
        PROCESSORS.get(mqttMessage.fixedHeader().messageType().value()).process(channelHandlerContext, mqttMessage);
    }

}
