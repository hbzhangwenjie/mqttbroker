package com.zwj.mqtt.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * @Author: zwj
 * @Date: 2019-11-18 16:55
 *
 * mqtt 协议的消息类型
 */

public interface MqttProtocol {

    /**
     * 实现类具体处理的消息类型
     */
    int getMessageType();

    /**
     * 处理mqtt消息
     */
    void process(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage);
}
