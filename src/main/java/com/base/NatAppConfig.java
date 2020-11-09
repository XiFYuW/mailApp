package com.base;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component("natAppConfig")
@Configuration
@ConfigurationProperties(prefix = "nat-app")
public class NatAppConfig {
    private String log;

    private String format;

    private String pan;

    private String performStart;

    private String performStop;

    public NatAppConfig() {
    }

    public NatAppConfig(String log, String format, String pan, String performStart, String performStop) {
        this.log = log;
        this.format = format;
        this.pan = pan;
        this.performStart = performStart;
        this.performStop = performStop;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPerformStart() {
        return performStart;
    }

    public void setPerformStart(String performStart) {
        this.performStart = performStart;
    }

    public String getPerformStop() {
        return performStop;
    }

    public void setPerformStop(String performStop) {
        this.performStop = performStop;
    }
}
