package com.nest.ib.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ListenerAsyncConfiguration implements AsyncConfigurer {

    /**
     * Gets the asynchronous thread pool execution object
     *
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        // Use the Spring built-in thread pool task object
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // Set the thread pool parameters
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
