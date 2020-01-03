package com.zwj.mqtt.security;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: zwj
 * @Date: 2019-11-18 14:21
 */
public class CustomSslcontextBuilder {

    private final static String SERVER_CRT = "/security/server/server.crt";
    private final static String SERVER_KEY = "/security/server/pkcs8_server.key";
    private final static String CA_CRT = "/security/ca.crt";
    private final static String KEY_PASSWORD = null;

    public static SslContext buildServer(ClientAuth clientAuth) throws IOException {
        InputStream certInput = null;
        InputStream priKeyInput = null;
        InputStream caInput = null;
        try {
            certInput = CustomSslcontextBuilder.class.getResourceAsStream(SERVER_CRT);
            priKeyInput = CustomSslcontextBuilder.class.getResourceAsStream(SERVER_KEY);
            caInput = CustomSslcontextBuilder.class.getResourceAsStream(CA_CRT);
            return SslContextBuilder.forServer(certInput, priKeyInput, KEY_PASSWORD)
                    .clientAuth(clientAuth)
                    .trustManager(caInput).build();
        } catch (Throwable e) {
            throw new RuntimeException("CustomSslcontextBuilder failed", e);
        } finally {
            if (certInput != null) {
                certInput.close();
            }
            if (priKeyInput != null) {
                priKeyInput.close();
            }
            if (caInput != null) {
                caInput.close();
            }
        }
    }
}
