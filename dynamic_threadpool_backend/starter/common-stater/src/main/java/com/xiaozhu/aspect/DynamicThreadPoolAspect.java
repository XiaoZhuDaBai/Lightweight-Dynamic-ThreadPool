package com.xiaozhu.aspect;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2026/1/14 18:05
 */

import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.executor.ThreadPoolExecutorProperties;
import com.xiaozhu.executor.XiaozhuThreadExecutor;
import com.xiaozhu.executor.XiaozhuThreadRegistry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池AOP切面实现
 * 负责收集所有@DynamicThreadPool注解的线程池，延迟注册
 */
@Slf4j
@Aspect
public class DynamicThreadPoolAspect implements ApplicationContextAware {

    // 存储待注册的线程池，key为threadPoolId，value为XiaozhuThreadExecutor
    private static final ConcurrentMap<String, XiaozhuThreadExecutor> PENDING_REGISTRATIONS =
            new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
    }

    @Around("@annotation(com.xiaozhu.DynamicThreadPool)")
    public Object collectDynamicThreadPool(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行原方法，获取线程池实例
        Object result = joinPoint.proceed();

        // 检查返回结果是否是XiaozhuThreadExecutor
        if (result instanceof XiaozhuThreadExecutor executor) {
            String threadPoolId = executor.getThreadPoolId();

            // 将线程池添加到待注册集合中
            PENDING_REGISTRATIONS.put(threadPoolId, executor);
            log.debug("收集动态线程池: {} (核心: {}, 最大: {})",
                    threadPoolId, executor.getCorePoolSize(), executor.getMaximumPoolSize());
        }

        return result;
    }

    /**
     * 注册所有收集到的动态线程池
     * 应该在ApplicationContext完全初始化后调用
     */
    public static void registerAllPendingThreadPools() {
        if (PENDING_REGISTRATIONS.isEmpty()) {
            log.debug("没有待注册的动态线程池");
            return;
        }

        log.info("开始注册 {} 个动态线程池", PENDING_REGISTRATIONS.size());

        for (XiaozhuThreadExecutor executor : PENDING_REGISTRATIONS.values()) {
            try {
                registerThreadPool(executor);
            } catch (Exception e) {
                log.error("注册线程池 {} 失败", executor.getThreadPoolId(), e);
            }
        }

        PENDING_REGISTRATIONS.clear();
    }

    /**
     * 注册单个线程池
     */
    private static void registerThreadPool(XiaozhuThreadExecutor executor) {
        String threadPoolId = executor.getThreadPoolId();

        // 检查是否已经注册过
        if (XiaozhuThreadRegistry.getHolder(threadPoolId) != null) {
            log.debug("线程池 {} 已经注册，跳过", threadPoolId);
            return;
        }

        // 从YAML配置中获取对应的配置
        BootstrapConfigProperties globalConfig =
                BootstrapConfigProperties.getInstance();

        ThreadPoolExecutorProperties yamlProperties =
                findYamlProperties(threadPoolId, globalConfig);

        if (yamlProperties != null) {
            // 使用YAML中的配置，合并实际的线程池参数
            ThreadPoolExecutorProperties properties =
                    ThreadPoolExecutorProperties.builder()
                    .threadPoolId(threadPoolId)
                    .corePoolSize(executor.getCorePoolSize())
                    .maximumPoolSize(executor.getMaximumPoolSize())
                    .keepAliveTime(executor.getKeepAliveTime(TimeUnit.SECONDS))
                    .workQueue(yamlProperties.getWorkQueue())
                    .rejectedHandler(yamlProperties.getRejectedHandler())
                    .queueCapacity(yamlProperties.getQueueCapacity())
                    .allowCoreThreadTimeOut(yamlProperties.getAllowCoreThreadTimeOut())
                    .notify(yamlProperties.getNotify())
                    .alarm(yamlProperties.getAlarm())
                    .build();

            // 注册到动态线程池系统
            XiaozhuThreadRegistry.register(threadPoolId, executor, properties);
            log.info("动态线程池注册成功: {} (核心: {}, 最大: {})",
                    threadPoolId, executor.getCorePoolSize(), executor.getMaximumPoolSize());
        } else {
            log.error("YAML配置中未找到线程池 '{}' 的配置，无法注册。请检查配置", threadPoolId);
        }
    }

    /**
     * 从全局配置中查找对应的线程池配置
     */
    private static ThreadPoolExecutorProperties findYamlProperties(String threadPoolId, BootstrapConfigProperties globalConfig) {
        if (globalConfig != null && globalConfig.getExecutors() != null) {
            return globalConfig.getExecutors().stream()
                    .filter(props -> threadPoolId.equals(props.getThreadPoolId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }


}
