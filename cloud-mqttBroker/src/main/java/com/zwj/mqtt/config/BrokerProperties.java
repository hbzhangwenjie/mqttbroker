package com.zwj.mqtt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zwj
 * @Date: 2019-11-19 14:59
 */
@ConfigurationProperties(prefix = "zwj.mqtt")
@EnableConfigurationProperties(BrokerProperties.class)
@Configuration
public class BrokerProperties {

    private Integer brokerId;
    private String host = "127.0.0.1";
    private Integer port = 9090;
    private Integer backPort = 9089;
    private String messageSyncTopic;
    private String clusterTopic;
    private Integer bossThread;
    private Integer workThread;
    private Integer backBossThread;
    private Integer backWorkThread;
    /**
     * linux 系统才能使用epoll
     */
    private Boolean useEpoll = false;
    private Boolean sslEnable = false;
    private Boolean autoSub = false;
    private Integer readerIdleTimeSeconds = 0;
    private Integer writerIdleTimeSeconds = 0;
    private Integer allIdleTimeSeconds = 60;

    public String getMessageSyncTopic() {
        return messageSyncTopic;
    }

    public void setMessageSyncTopic(String messageSyncTopic) {
        this.messageSyncTopic = messageSyncTopic;
    }

    public String getClusterTopic() {
        return clusterTopic;
    }

    public void setClusterTopic(String clusterTopic) {
        this.clusterTopic = clusterTopic;
    }

    public Integer getBackPort() {
        return backPort;
    }

    public void setBackPort(Integer backPort) {
        this.backPort = backPort;
    }

    public Integer getBossThread() {
        return bossThread;
    }

    public void setBossThread(Integer bossThread) {
        this.bossThread = bossThread;
    }

    public Integer getWorkThread() {
        return workThread;
    }

    public void setWorkThread(Integer workThread) {
        this.workThread = workThread;
    }

    public Integer getBackBossThread() {
        return backBossThread;
    }

    public void setBackBossThread(Integer backBossThread) {
        this.backBossThread = backBossThread;
    }

    public Integer getBackWorkThread() {
        return backWorkThread;
    }

    public void setBackWorkThread(Integer backWorkThread) {
        this.backWorkThread = backWorkThread;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getUseEpoll() {
        return useEpoll;
    }

    public void setUseEpoll(Boolean useEpoll) {
        this.useEpoll = useEpoll;
    }

    public Boolean getSslEnable() {
        return sslEnable;
    }

    public Boolean getAutoSub() {
        return autoSub;
    }

    public void setAutoSub(Boolean autoSub) {
        this.autoSub = autoSub;
    }

    public void setSslEnable(Boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public Integer getReaderIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeSeconds(Integer readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public Integer getWriterIdleTimeSeconds() {
        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeSeconds(Integer writerIdleTimeSeconds) {
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public Integer getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeSeconds(Integer allIdleTimeSeconds) {
        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }
}
