package com.zwj.mqtt.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;

/**
 * @Author: zwj
 * @Date: 2019-11-18 19:24
 *
 * 服务端会收到的mqtt消息类型
 */
public abstract class AbstractMqttProtocol implements MqttProtocol {

    /**
     * 连接服务端：客户端发送给服务端的第一个报文必须是CONNECT报文
     */
    public void connect(Channel channel, MqttConnectMessage msg) {

    }

    /**
     * 确认连接请求：服务端发送CONNACK报文来响应从客户端收到的CONNECT报文。 服务端发送给客户端的第一个报文必须是CONNACK。如果客户端在合理的时间内 没有收到服务端的CONNACK报文，客户端应该关闭网络连接。合理的时间取决于 应用的类型和通信基础设施。
     */
    public void connectAck(Channel channel, MqttConnectMessage msg) {

    }

    /**
     * 发布消息：PUBLISH控制报文是指从客户端向服务端或者服务端向客户端传输一个应用消息。
     */
    public void publish(Channel channel, MqttPublishMessage msg) {

    }

    /**
     * 发布确认：PUBACK报文是对QoS 1等级的PUBLISH报文的响应。
     */
    public void pubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
    }

    /**
     * 发布收到（ QoS 2， 第一步）：PUBREC报文是对QoS等级2的PUBLISH报文的响应。 它是QoS 2等级协议交换的第二个报文。
     */
    public void pubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }

    /**
     * 发布释放（ QoS 2， 第二步）：PUBREL报文是对PUBREC报文的响应。 它是QoS 2等级协议交换的第三个报文。
     */
    public void pubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }

    /**
     * 发布完成（ QoS 2， 第三步）：PUBCOMP报文是对PUBREL报文的响应。 它是QoS 2等级协议交换的第四个也是最后一个报文。
     */
    public void pubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {

    }

    /**
     * 订阅主题：客户端向服务端发送SUBSCRIBE报文用于创建一个或多个订阅。 每个订阅注册客户端关心的一个或多个主题。 为了将应用消息转发给与那些订阅匹配的主题， 服务端发送PUBLISH报文给客户端。 SUBSCRIBE报文也（ 为每个订阅） 指定了最大的QoS等级，
     * 服务端根据这个发送应用消息给客户端。
     */
    public void subscribe(Channel channel, MqttSubscribeMessage msg) {

    }

    /**
     * 订阅确认：服务端发送SUBACK报文给客户端， 用于确认它已收到并且正在处理SUBSCRIBE报文。
     */
    public void subAck(Channel channel, MqttSubscribeMessage msg) {

    }

    /**
     * 取消订阅：客户端发送UNSUBSCRIBE报文给服务端， 用于取消订阅主题
     */
    public void unSubscribe(Channel channel, MqttUnsubscribeMessage msg) {

    }

    /**
     * 取消订阅确认：服务端发送UNSUBACK报文给客户端用于确认收到UNSUBSCRIBE报文。
     */
    public void unSubAsk(Channel channel, MqttUnsubscribeMessage msg) {

    }

    /**
     * 心跳请求：客户端发送PINGREQ报文给服务端的。 用于： 1. 在没有任何其它控制报文从客户端发给服务的时， 告知服务端客户端还活着。 2. 请求服务端发送 响应确认它还活着。 3. 使用网络以确认网络连接没有断开。
     */
    public void pingReq(Channel channel, MqttMessage msg) {
    }


    /**
     * 心跳响应：服务端发送PINGRESP报文响应客户端的PINGREQ报文。 表示服务端还活着。
     */
    public void pingResp(Channel channel, MqttMessage msg) {
    }

    /**
     * 断开连接：DISCONNECT报文是客户端发给服务端的最后一个控制报文。 表示客户端正常断开连接。
     */
    public void disConnect(Channel channel, MqttMessage msg) {
    }
}
