package com.zwj.mqtt.service;

import java.util.Set;

/**
 * @Author: zwj
 * @Date: 2019-11-22 14:48
 */
public interface MessageIdService {

    void putMessageId(String clientId, int messageId);

    void delMessageIdByMessageId(String clientId, int messageId);

    boolean containMessageId(String clientId, int messageId);

    void delMessageId(String clientId);

    Set<String> getMessageId(String clientId);

}
