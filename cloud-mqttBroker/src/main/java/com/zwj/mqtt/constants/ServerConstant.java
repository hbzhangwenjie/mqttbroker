package com.zwj.mqtt.constants;

import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zwj
 * @Date: 2019-11-19 14:55
 */
public class ServerConstant {

    public static final ConcurrentHashMap<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
}
