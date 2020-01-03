package com.zwj.mqtt.service;

import com.zwj.mqtt.model.SessionModel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * @Author: zwj
 * @Date: 2019-11-19 14:26
 */
public interface SessionService {


    boolean containClientId(String clientId);

    SessionModel getSessionByClientId(String client);


    void removeByClientId(String clientId);

    /**
     * @param clientId
     * @param sessionModel
     */
    void putSessionByClient(String clientId,SessionModel sessionModel);


}
