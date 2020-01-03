package com.zwj.mqtt.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @Author: zwj
 * @Date: 2019-11-19 16:01
 */
public class GsonSerialize {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public static Gson getGSON() {
        return GSON;
    }
}
