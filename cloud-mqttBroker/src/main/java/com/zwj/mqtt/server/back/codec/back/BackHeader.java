package com.zwj.mqtt.server.back.codec.back;

import lombok.ToString;

/**
 * @Author: zwj
 * @Date: 2019-12-12 15:03
 */
@ToString
public class BackHeader {
    /**
     * back消息校验码 总共4个字节 前2个字节 固定值 bacf 表明这是back协议的消息 次低字节标示主版本号 低字节标示次版本号
     */
    private int crcCode = 0xbacf0101;
    /**
     * 消息类型
     */
    private byte type;
    /**
     * 消息的长度，不包括消息头
     */
    private int length;

    public int getCrcCode() {
        return crcCode;
    }

    public void setCrcCode(int crcCode) {
        this.crcCode = crcCode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
