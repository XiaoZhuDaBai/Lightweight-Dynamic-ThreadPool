package com.xiaozhu.monitor;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/17 22:48
 */

import lombok.Builder;

/**
 * 创建不可变的运行信息对象
 * @param threadPoolId
 * @param corePoolSize
 * @param maximumPoolSize
 * @param currentPoolSize
 * @param activePoolCount
 * @param largestPoolSize
 * @param completedTaskCount    线程池任务总量
 * @param workQueueName
 * @param workQueueCapacity
 * @param workQueueSize
 * @param workQueueRemainingCapacity    队列剩余容量
 * @param rejectedHandlerName
 * @param rejectedCount     拒绝策略调用次数
 */
@Builder
public record ThreadPoolRuntimeInfo(
        String threadPoolId,
        Integer corePoolSize,
        Integer maximumPoolSize,
        Integer currentPoolSize,
        Integer activePoolCount,
        Integer largestPoolSize,
        Long completedTaskCount,
        String workQueueName,
        Integer workQueueCapacity,
        Integer workQueueSize,
        Integer workQueueRemainingCapacity,
        String rejectedHandlerName,
        Long rejectedCount
) {
}
