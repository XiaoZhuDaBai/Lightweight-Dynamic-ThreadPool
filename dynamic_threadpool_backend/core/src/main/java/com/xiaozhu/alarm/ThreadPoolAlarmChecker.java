package com.xiaozhu.alarm;

import cn.hutool.core.date.DateUtil;
import com.xiaozhu.builder.ThreadFactoryBuilder;
import com.xiaozhu.config.ApplicationProperties;
import com.xiaozhu.executor.ThreadPoolExecutorHolder;
import com.xiaozhu.executor.ThreadPoolExecutorProperties;
import com.xiaozhu.executor.XiaozhuThreadExecutor;
import com.xiaozhu.executor.XiaozhuThreadRegistry;
import com.xiaozhu.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.xiaozhu.notification.service.NotifierDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:22
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolAlarmChecker implements DisposableBean {
    private final Map<String, Long> lastRejectCountMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder.builder()
                    .namePrefix("scheduler_thread-pool_alarm_checker")
                    .build()
    );

    private final NotifierDispatcher notifierDispatcher;

    /**
     * 启动定时检查任务
     */
    public void start() {
        // 每10秒检查一次，初始延迟0秒
        scheduler.scheduleWithFixedDelay(this::checkAlarm, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        stop();
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("ThreadPoolAlarmChecker 关闭被中断");
            }
        }
    }

    /**
     * 报警检查核心逻辑
     */
    private void checkAlarm() {
        //从注册中心拿到线程池管理器
        Collection<ThreadPoolExecutorHolder> holders = XiaozhuThreadRegistry.getAllHolders();
        for (ThreadPoolExecutorHolder holder : holders) {
            // 开启监控才配置
            if (holder.getExecutorProperties().getAlarm().getEnable()) {
                checkQueueUsage(holder);
                checkActiveRate(holder);
                checkRejectCount(holder);
            }
        }
    }

    /**
     * 队列使用率检查
     * @param holder
     */
    private void checkQueueUsage(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties executorProperties = holder.getExecutorProperties();

        BlockingQueue<?> queue = executor.getQueue();
        int workQueueSize = queue.size();
        // 总容量
        int capacity = workQueueSize + queue.remainingCapacity();

        if (capacity == 0) {
            return;
        }

        int usageRate = (int) Math.round(workQueueSize * 100.0 / capacity);
        // 队列阈值
        Integer queueThreshold = executorProperties.getAlarm().getQueueThreshold();
        if (usageRate >= queueThreshold) {
            sendAlarmMessage("Capacity", holder);
        }
    }

    /**
     * 检查线程活跃度（活跃线程数 / 最大线程数）
     * @param holder
     */
    private void checkActiveRate(ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties executorProperties = holder.getExecutorProperties();

        int activeCount = executor.getActiveCount(); // API 有锁，避免高频率调用
        int maximumPoolSize = executor.getMaximumPoolSize();

        if (maximumPoolSize == 0) {
            return;
        }

        int usageRate = (int) Math.round(activeCount * 100.0 / maximumPoolSize);
        // 活跃阈值
        Integer activeThreshold = executorProperties.getAlarm().getActiveThreshold();

        if (usageRate >= activeThreshold) {
            sendAlarmMessage("Active", holder);
        }
    }

    /**
     * 检查拒绝策略执行次数
     */
    private void checkRejectCount(ThreadPoolExecutorHolder holder) {
        String threadPoolId = holder.getThreadPoolId();
        ThreadPoolExecutor executor = holder.getExecutor();

        if (!(executor instanceof XiaozhuThreadExecutor xiaozhuThreadExecutor)) {
            return;
        }

        long currentRejectCount = xiaozhuThreadExecutor.getRejectCount().get();
        // 上一次的拒绝次数
        long lastRejectCount = lastRejectCountMap.getOrDefault(threadPoolId, 0L);

        // 只有拒绝策略执行次数有变化时才发送报警消息
        if (currentRejectCount > lastRejectCount) {
            sendAlarmMessage("Reject", holder);
            lastRejectCountMap.put(threadPoolId, currentRejectCount);
        }

    }


    private void sendAlarmMessage(String alarmType, ThreadPoolExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        ThreadPoolExecutorProperties properties = holder.getExecutorProperties();
        BlockingQueue<?> queue = executor.getQueue();

        long rejectCount = -1L;
        if (executor instanceof XiaozhuThreadExecutor) {
            rejectCount = ((XiaozhuThreadExecutor) executor).getRejectCount().get();
        }

        int workQueueSize = queue.size();
        int remainingCapacity = queue.remainingCapacity();

        String identify;
        try {
            identify = InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e) {
            log.warn("无法获取本地主机地址，使用默认值: {}", e.getMessage());
            identify = "unknown";
        }

        ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
                .applicationName(ApplicationProperties.getApplicationName())
                .activeProfile(ApplicationProperties.getActiveProfile())
                .identify(identify)
                .alarmType(alarmType)
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolSize(executor.getActiveCount())
                .currentPoolSize(executor.getPoolSize())
                .completedTaskCount(executor.getCompletedTaskCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(workQueueSize)
                .workQueueRemainingCapacity(remainingCapacity)
                .workQueueCapacity(workQueueSize + remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectCount(rejectCount)
                .receives(properties.getNotify().getReceivers())
                .currentTime(DateUtil.now())
                .interval(properties.getNotify().getInterval())
                .build();

        notifierDispatcher.sendAlarmMessage(alarm);
    }
}
