package com.sword.batch.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnMissingBean(TaskExecutor.class)
public class TaskExecutorConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new  ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(env.getProperty("batch.core.pool.size", Integer.class, 5));
        taskExecutor.setQueueCapacity(env.getProperty("batch.queue.capacity", Integer.class, Integer.MAX_VALUE));
        taskExecutor.setMaxPoolSize(env.getProperty("batch.max.pool.size", Integer.class, Integer.MAX_VALUE));
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

}

