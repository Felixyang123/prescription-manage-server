package com.myproject.prescription.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@Configuration
public class AsyncExecutorConfig {
    @Bean
    public ThreadPoolTaskExecutor asyncDefaultExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(processors);
        taskExecutor.setMaxPoolSize(processors * 2);
        taskExecutor.setQueueCapacity(10000);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return taskExecutor;
    }
}
