package com.example.demo.config;

import com.xiaozhu.DynamicThreadPool;
import com.xiaozhu.builder.ThreadPoolExecutorBuilder;
import com.xiaozhu.configuration.CommonAutoConfiguration;
import com.xiaozhu.executor.myenum.BlockingQueueTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态线程池配置
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2026/1/12 22:06
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicThreadPoolConfiguration {

    /**
     * 生产者线程池Bean定义
     * 注意：这里只是创建Bean，不进行动态注册
     */
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor onethreadProducer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("onethread-producer")
                .corePoolSize(2)
                .maximumPoolSize(4)
                .keepAliveTime(9999L)
                .awaitTerminationMillis(5000L)
                .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
                .threadFactory("onethread-producer_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .allowCoreThreadTimeOut(false)
                .dynamicPool()
                .build();
    }

    /**
     * 消费者线程池Bean定义
     */
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor onethreadConsumer() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("onethread-consumer")
                .corePoolSize(4)
                .maximumPoolSize(6)
                .keepAliveTime(9999L)
                .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
                .threadFactory("onethread-consumer_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .allowCoreThreadTimeOut(false)
                .dynamicPool()
                .build();
    }

    /**
     * 订单处理线程池Bean定义
     */
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor orderExecutor() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("order-executor")
                .corePoolSize(5)
                .maximumPoolSize(20)
                .keepAliveTime(60L)
                .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
                .threadFactory("order-executor_")
                .rejectedHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .allowCoreThreadTimeOut(false)
                .dynamicPool()
                .build();
    }

    /**
     * 用户处理线程池Bean定义
     */
    @Bean
    @DynamicThreadPool
    public ThreadPoolExecutor userExecutor() {
        return ThreadPoolExecutorBuilder.builder()
                .threadPoolId("user-executor")
                .corePoolSize(3)
                .maximumPoolSize(15)
                .keepAliveTime(30L)
                .workQueueType(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE)
                .threadFactory("user-executor_")
                .rejectedHandler(new ThreadPoolExecutor.DiscardOldestPolicy())
                .allowCoreThreadTimeOut(false)
                .dynamicPool()
                .build();
    }
}
