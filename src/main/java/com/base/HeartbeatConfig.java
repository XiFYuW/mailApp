package com.base;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component("heartbeatConfig")
@ConfigurationProperties(prefix = "heartbeat")
@Configuration
public class HeartbeatConfig {

    private String local;

    private Long initialDelay;

    private Long delay;

    public HeartbeatConfig() {
    }

    public HeartbeatConfig(String local, Long initialDelay, Long delay) {
        this.local = local;
        this.initialDelay = initialDelay;
        this.delay = delay;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }
}
