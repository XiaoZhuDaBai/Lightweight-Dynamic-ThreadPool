package com.xiaozhu.executor;

import com.xiaozhu.executor.aop.RejectedProxyInvocationHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/15 23:24
 */
@Slf4j
public class XiaozhuThreadExecutor extends ThreadPoolExecutor {

    /**
     * 线程池的唯一id
     */
    @Getter
    private final String threadPoolId;
    /**
     * 拒绝策略拒绝次数
     */
    @Getter
    private final AtomicLong rejectCount = new AtomicLong();
    /**
     * 等待终止时间，单位毫秒
     */
    private final long awaitTerminationMillis;

    public XiaozhuThreadExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        setRejectedExecutionHandler(handler);
        this.threadPoolId = threadPoolId;
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    /**
     * 利用动态代理机制设置拒绝策略
     * @param handler the new handler
     */
    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        RejectedExecutionHandler rejectedProxy = (RejectedExecutionHandler) Proxy
                .newProxyInstance(
                        handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedProxyInvocationHandler(handler, rejectCount)
                );
        super.setRejectedExecutionHandler(rejectedProxy);
    }

    /**
     * 关闭线程池
     */
    @Override
    public void shutdown() {
        if (isShutdown()) {
            return;
        }
        super.shutdown();
        // 等待终止时间小于等于0，不进行等待
        if (this.awaitTerminationMillis <= 0) {
            return;
        }
        // 需要等待
        log.info("关闭前还在执行的ExecutorService {}", threadPoolId);
        // 关闭
        try {
            boolean awaitTermination = this.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS);
            if (!awaitTermination) {
                log.warn("等待该ExecutorService {} 终止执行.", threadPoolId);
            } else {
                log.info("ExecutorService {} 已经关闭.", threadPoolId);
            }
        } catch (InterruptedException e) {
            log.warn("等待该ExecutorService {} 终止，受到阻碍.", threadPoolId);
            Thread.currentThread().interrupt();
        }
    }
}
