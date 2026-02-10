package com.xiaozhu.builder;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/17 17:27
 */

/**
 * 建造者模式
 */
public class ThreadFactoryBuilder {
    private ThreadFactory threadFactory;
    private String namePrefix;
    // 是否是守护线程, 默认false
    private Boolean daemon;
    // 优先级
    private Integer priority;
    /**
     * 未捕获异常处理器
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 创建 ThreadFactoryBuilder 实例
     */
    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    public ThreadFactoryBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }
    public ThreadFactoryBuilder namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }
    public ThreadFactoryBuilder daemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }
    // 这里需要对参数做校验
    public ThreadFactoryBuilder priority(Integer priority) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("线程的优先级必须在1到10之间。");
        }
        this.priority = priority;
        return this;
    }
    public ThreadFactoryBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    public ThreadFactory build() {
        final ThreadFactory factory = (this.threadFactory != null) ?
                this.threadFactory :
                Executors.defaultThreadFactory();
        Assert.notEmpty(namePrefix, "名字前缀不能为null或空");
        final AtomicLong count = (StrUtil.isNotBlank(namePrefix)) ? new AtomicLong(0) : null;

        return (runnable) -> {
            Thread thread = factory.newThread(runnable);

            if (count != null) {
                thread.setName(namePrefix + count.getAndIncrement());
            }

            if (daemon != null) {
                thread.setDaemon(daemon);
            }

            if (priority != null) {
                thread.setPriority(priority);
            }

            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }

            return thread;
        };
    }
}
