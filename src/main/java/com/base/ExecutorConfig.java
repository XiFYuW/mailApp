package com.base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean
    public ScheduledExecutorService sendMailServiceExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new SendMailThreadFactory("sendMailServiceExecutor_"));
    }

    @Bean
    public ScheduledExecutorService sendMailTimeServiceExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new SendMailThreadFactory("sendMailTimeServiceExecutor_", true));
    }

}
