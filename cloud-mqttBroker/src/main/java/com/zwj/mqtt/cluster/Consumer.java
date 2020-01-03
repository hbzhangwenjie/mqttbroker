package com.zwj.mqtt.cluster;

import static com.zwj.mqtt.constants.ServerConstant.CHANNEL_MAP;

import com.zwj.mqtt.config.BrokerProperties;
import com.zwj.mqtt.protocol.MqttServerPublishProcess;
import com.zwj.mqtt.serialize.GsonSerialize;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Author: zwj
 * @Date: 2019-12-02 15:46
 */
@Component
@Slf4j
public class Consumer {

    @Autowired
    private BrokerProperties brokerProperties;
    @Autowired
    private MqttServerPublishProcess mqttServerPublishProcess;

    @KafkaListener(topics = "${zwj.mqtt.message-sync-topic}", groupId = "${spring.kafka.consumer.group-id}-${zwj.mqtt.brokerId}")
    public void onMessage(String message) {
        MessageDto messageDto = GsonSerialize.getGSON().fromJson(message, MessageDto.class);
        String clientId = messageDto.getClientId();
        Channel channel;
        log.debug("received kafka message:{}", message);
        if ((channel = CHANNEL_MAP.get(clientId)) != null) {
            mqttServerPublishProcess.doPublish(messageDto.getClientId(), MqttQoS.valueOf(messageDto.getMqttQoS()), channel, messageDto.getTopic(),
                    messageDto.getPayload().getBytes(StandardCharsets.UTF_8), messageDto.isRetain(), messageDto.isDup());
        }
    }

    @KafkaListener(topics = "${zwj.mqtt.cluster-topic}", groupId = "${spring.kafka.consumer.group-id}-${zwj.mqtt.brokerId}")
    public void onConnect(String message) {
        String[] strings = message.split("_");
        //不是自己发出的
        if (!strings[0].equalsIgnoreCase(brokerProperties.getBrokerId().toString())) {
            Channel channelOld = CHANNEL_MAP.get(strings[1]);
            if (channelOld != null) {
                //关闭之前的连接
                channelOld.close();
            }
        }
    }
}
