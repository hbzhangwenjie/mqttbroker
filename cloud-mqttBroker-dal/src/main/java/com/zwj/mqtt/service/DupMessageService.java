package com.zwj.mqtt.service;

import com.zwj.mqtt.model.DupMessageModel;
import java.util.Map;

/**
 * @Author: zwj
 * @Date: 2019-11-22 14:02
 */
public interface DupMessageService {


    void putDupMessage(int messageId, String clientId, DupMessageModel dupMessageModel);

    DupMessageModel getDupMessageByMessageId(int messageId, String clientId);

    void deleteDupMessageByClientId(String clientId);


    void deleteDupMessageByMessageIdAndClientId(int messageId, String clientId);

    Map<Object, Object> getDupMessageModelByClientId(String clientId);

}
