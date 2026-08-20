package com.nhnacademy.ruleengine.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService flowExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
