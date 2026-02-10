package com.xiaozhu.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/15 23:27
 */
@Data
@AllArgsConstructor
public class ThreadPoolExecutorHolder {
    /**
     * 线程池的唯一id
     */
    @Getter
    private String threadPoolId;
    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * 线程池配置信息
     */
    private ThreadPoolExecutorProperties executorProperties;
}
