package com.qoobot.openrecommend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 *
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${thread-pool.core-pool-size:10}")
    private int corePoolSize;

    @Value("${thread-pool.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${thread-pool.queue-capacity:200}")
    private int queueCapacity;

    @Value("${thread-pool.thread-name-prefix:async-task-}")
    private String threadNamePrefix;

    /**
     * 虚拟线程执行器（JDK 21+）
     */
    @Bean("virtualThreadExecutor")
    public ExecutorService virtualThreadExecutor() {
        log.info("初始化虚拟线程执行器");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 异步任务线程池
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        log.info("初始化异步任务线程池，核心线程数: {}, 最大线程数: {}, 队列容量: {}",
                corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        return executor;
    }
}
