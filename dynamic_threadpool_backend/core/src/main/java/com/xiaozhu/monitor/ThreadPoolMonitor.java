package com.xiaozhu.monitor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.xiaozhu.builder.ThreadFactoryBuilder;
import com.xiaozhu.config.ApplicationProperties;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.executor.ThreadPoolExecutorHolder;
import com.xiaozhu.executor.XiaozhuThreadExecutor;
import com.xiaozhu.executor.XiaozhuThreadRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/18 15:52
 */
@Slf4j
public class ThreadPoolMonitor {
    private ScheduledExecutorService scheduler;
    private Map<String, ThreadPoolRuntimeInfo> micrometerMonitorCache;

    private static final String METRIC_NAME_PREFIX = "dynamic.thread-pool";
    private static final String DYNAMIC_THREAD_POOL_ID_TAG = METRIC_NAME_PREFIX + ".id";
    private static final String APPLICATION_NAME_TAG = "application.name";
    /**
     * 启动定时检查任务
     */
    public void start() {
        BootstrapConfigProperties.MonitorConfig monitorConfig = BootstrapConfigProperties.getInstance().getMonitorConfig();
        // 不启用监控
        if (!monitorConfig.getEnable()) {
            return;
        }

        // 初始化监控相关资源
        micrometerMonitorCache = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(
                1,
                ThreadFactoryBuilder.builder()
                        .namePrefix("scheduler_thread-pool_monitor")
                        .build()
        );

        // 启动定时任务
        scheduler.scheduleWithFixedDelay(() -> {
                // 拿到所有线程池
            Collection<ThreadPoolExecutorHolder> holders = XiaozhuThreadRegistry.getAllHolders();
            for (ThreadPoolExecutorHolder holder : holders) {
                ThreadPoolRuntimeInfo threadPoolRuntimeInfo = buildThreadPoolRuntimeInfo(holder);
                //监控方式
                if (Objects.equals(monitorConfig.getCollectType(), "log")) {
                    // 日志监控
                    logMonitor(threadPoolRuntimeInfo);
                } else if (Objects.equals(monitorConfig.getCollectType(), "micrometer")) {
                    // micrometer 监控
                    micrometerMonitor(threadPoolRuntimeInfo);
                }
            }
        }, 0, monitorConfig.getCollectInterval(), TimeUnit.SECONDS);
    }


    /**
     * 停止定时检查任务
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**\
     * micrometer 监控
     * @param runtimeInfo
     */
    private void micrometerMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        String threadPoolId = runtimeInfo.threadPoolId();
        ThreadPoolRuntimeInfo existingRuntimeInfo = micrometerMonitorCache.get(threadPoolId);
        if (existingRuntimeInfo != null) {
            BeanUtil.copyProperties(runtimeInfo, existingRuntimeInfo);
        } else {
            micrometerMonitorCache.put(threadPoolId, runtimeInfo);
        }
        Iterable<Tag> tags = CollectionUtil.newArrayList(
                Tag.of(DYNAMIC_THREAD_POOL_ID_TAG, threadPoolId),
                Tag.of(APPLICATION_NAME_TAG, ApplicationProperties.getApplicationName()));

        Metrics.gauge(metricName("core.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::corePoolSize);
        Metrics.gauge(metricName("maximum.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::maximumPoolSize);
        Metrics.gauge(metricName("current.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::currentPoolSize);
        Metrics.gauge(metricName("largest.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::largestPoolSize);
        Metrics.gauge(metricName("active.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::activePoolCount);
        Metrics.gauge(metricName("queue.size"), tags, runtimeInfo, ThreadPoolRuntimeInfo::workQueueSize);
        Metrics.gauge(metricName("queue.capacity"), tags, runtimeInfo, ThreadPoolRuntimeInfo::workQueueCapacity);
        Metrics.gauge(metricName("queue.remaining.capacity"), tags, runtimeInfo, ThreadPoolRuntimeInfo::workQueueRemainingCapacity);
        Metrics.gauge(metricName("completed.task.count"), tags, runtimeInfo, ThreadPoolRuntimeInfo::completedTaskCount);
        Metrics.gauge(metricName("reject.count"), tags, runtimeInfo, ThreadPoolRuntimeInfo::rejectedCount);
    }


    private void logMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        log.info("[ThreadPool Monitor] {} | Content: {}", runtimeInfo.threadPoolId(), JSON.toJSON(runtimeInfo));
    }

    private String metricName(String name) {
        return String.join(".", METRIC_NAME_PREFIX, name);
    }

    @SneakyThrows
    private ThreadPoolRuntimeInfo buildThreadPoolRuntimeInfo(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BlockingQueue<?> queue = executor.getQueue();

        long rejectCount = -1L;
        if(executor instanceof XiaozhuThreadExecutor) {
            AtomicLong atomicLong = ((XiaozhuThreadExecutor) executor).getRejectCount();
            rejectCount = atomicLong.get();
        }

        int workQueueSize = queue.size(); // API 有锁，避免高频率调用
        int remainingCapacity = queue.remainingCapacity(); // API 有锁，避免高频率调用

        // 构建 ThreadPoolRuntimeInfo
        return ThreadPoolRuntimeInfo.builder()
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolCount(executor.getActiveCount())  // API 有锁，避免高频率调用
                .currentPoolSize(executor.getPoolSize())  // API 有锁，避免高频率调用
                .completedTaskCount(executor.getCompletedTaskCount())  // API 有锁，避免高频率调用
                .largestPoolSize(executor.getLargestPoolSize())  // API 有锁，避免高频率调用
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(workQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(workQueueSize + remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectedCount(rejectCount)
                .build();
    }
}
