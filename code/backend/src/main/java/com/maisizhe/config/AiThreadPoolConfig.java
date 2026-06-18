package com.maisizhe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI线程池配置
 * 用于异步处理AI任务，避免阻塞主线程
 * 
 * @author MaiSiZhe Team
 */
@Configuration
public class AiThreadPoolConfig {
    
    /**
     * AI任务队列(有界队列，防止内存溢出)
     */
    @Bean(name = "aiTaskQueue")
    public BlockingQueue<Runnable> aiTaskQueue() {
        return new LinkedBlockingQueue<>(500);
    }
    
    /**
     * AI工作线程池
     * 核心线程数: 4
     * 最大线程数: 8
     * 队列容量: 500
     * 线程名前缀: ai-worker-
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor(BlockingQueue<Runnable> aiTaskQueue) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,                          // 核心线程数
            8,                          // 最大线程数
            60L,                        // 空闲线程存活时间
            java.util.concurrent.TimeUnit.SECONDS,
            aiTaskQueue,                // 任务队列
            new java.util.concurrent.ThreadFactory() {
                private int count = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("ai-worker-" + (++count));
                    thread.setDaemon(true); // 守护线程
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
        );
        
        return executor;
    }
}
