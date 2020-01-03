package com.zwj.mqtt.service;

import com.zwj.mqtt.model.RetainMessageModel;

/**
 * @Author: zwj
 * @Date: 2019-12-03 16:04
 */
public interface RetainMessageService {

    void putRetainMessage(String topic, RetainMessageModel retainMessageModel);

    RetainMessageModel getRetainMessage(String topic);
}
