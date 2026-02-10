package com.xiaozhu.executor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/15 23:35
 */
@Data
@Builder
public class ThreadPoolExecutorProperties {
    /**
     * 线程池ID
     */
    private String threadPoolId;
    private Integer corePoolSize;
    private Integer maximumPoolSize;
    private Long keepAliveTime;
    /**
     * 阻塞队列类型
     */
    private String workQueue;
    /**
     * 拒绝策略类型
     */
    private String rejectedHandler;
    /**
     * 队列容量
     */
    private Integer queueCapacity;
    /**
     * 是否允许核心线程超时
     */
    private Boolean allowCoreThreadTimeOut;
    /**
     *  通知配置
     */
    private NotifyConfig notify;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {

        /**
         * 接收人集合
         */
        private String receivers;

        /**
         * 告警间隔，单位分钟
         */
        private Integer interval = 5;
    }



    /**
     *  报警配置，默认设置
     */
    private AlarmConfig alarm = new AlarmConfig();
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmConfig {

        /**
         * 默认开启报警配配置
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 队列阈值
         */
        private Integer queueThreshold = 80;

        /**
         * 活跃线程阈值
         */
        private Integer activeThreshold = 80;
    }
}
