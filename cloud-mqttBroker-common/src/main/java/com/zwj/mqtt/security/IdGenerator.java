package com.zwj.mqtt.security;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zwj
 * @Date: 2019-11-22 10:52
 */
public class IdGenerator {

    /**
     * 全局唯一的ID
     * brokerID占int 的15-11位，
     * 自增ID 占0-12
     * brokerId 可以是0-7
     * 这样brokerID=0 的 取之范围就是0-8191
     * 这样brokerID=1 的 取之范围就是8192-16383
     * 。。。
     * 这样brokerID=7 的取之范围就是57344-65535
     */
    private static final int SEQUENCE_MASK = 8191;
    private static final int SEQUENCE_BITS = 13;
    private static volatile AtomicInteger sequence = new AtomicInteger(0);

    public static int getMessageId(int brokerId) {
        return (sequence.getAndIncrement() & SEQUENCE_MASK) ^ (brokerId << SEQUENCE_BITS);
    }
}
