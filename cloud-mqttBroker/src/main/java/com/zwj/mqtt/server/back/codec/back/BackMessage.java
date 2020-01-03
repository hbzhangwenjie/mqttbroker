package com.zwj.mqtt.server.back.codec.back;

import lombok.ToString;

/**
 * @Author: zwj
 * @Date: 2019-12-12 15:03
 */
@ToString
public class BackMessage {

    private BackHeader backHeader;
    private String body;

    public BackHeader getBackHeader() {
        return backHeader;
    }

    public void setBackHeader(BackHeader backHeader) {
        this.backHeader = backHeader;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
