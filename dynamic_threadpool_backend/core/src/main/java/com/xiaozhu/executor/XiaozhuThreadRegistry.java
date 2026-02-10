package com.xiaozhu.executor;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/16 16:48
 */

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 注册到线程池管理处
 */
public class XiaozhuThreadRegistry {
    /**
     * 根据id存入 线程池Holder
     */
    private final static Map<String, ThreadPoolExecutorHolder> HOLDER_REGISTRY  = new ConcurrentHashMap<>();

    /**
     *  注册
     * @param threadPoolId
     * @param executor
     * @param properties
     */
    public static void register(String threadPoolId, ThreadPoolExecutor executor, ThreadPoolExecutorProperties properties) {
        ThreadPoolExecutorHolder threadPoolExecutorHolder = new ThreadPoolExecutorHolder(threadPoolId, executor, properties);
        HOLDER_REGISTRY.put(threadPoolId, threadPoolExecutorHolder);
    }

    public static ThreadPoolExecutorHolder getHolder(String threadPoolId) {
        return HOLDER_REGISTRY.get(threadPoolId);
    }

    public static Collection<ThreadPoolExecutorHolder> getAllHolders() {
        return HOLDER_REGISTRY.values();
    }
}
