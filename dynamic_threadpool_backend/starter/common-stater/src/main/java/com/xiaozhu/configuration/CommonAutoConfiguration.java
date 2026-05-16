package com.xiaozhu.configuration;

import com.xiaozhu.aspect.DynamicThreadPoolAspect;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.configuration.XiaozhuBootstrapConfigProperties;
import com.xiaozhu.enable.MarkerConfiguration;
import com.xiaozhu.notification.service.NotifierDispatcher;
import com.xiaozhu.refresh.DynamicThreadPoolRefreshListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:44
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XiaozhuBootstrapConfigProperties.class)
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@Import(XiaozhuBaseConfiguration.class)
@AutoConfigureAfter(XiaozhuBaseConfiguration.class)
@ConditionalOnProperty(prefix = "xiaozhu", value = "dynamicPool", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(XiaozhuBootstrapConfigProperties properties) {
        BootstrapConfigProperties.setInstance(properties);
        return properties;
    }

    @Bean
    public DynamicThreadPoolRefreshListener dynamicThreadPoolRefreshListener(NotifierDispatcher notifierDispatcher) {
        return new DynamicThreadPoolRefreshListener(notifierDispatcher);
    }

    /**
     * 动态线程池AOP切面
     * 自动注册所有标注@DynamicThreadPool的方法返回的线程池
     */
    @Bean
    public DynamicThreadPoolAspect dynamicThreadPoolAspect() {
        return new DynamicThreadPoolAspect();
    }

    /**
     * 应用启动完成后注册所有动态线程池
     * 等待 Nacos 配置加载后再注册，确保使用远程配置
     */
    @Bean
    public ApplicationRunner dynamicThreadPoolRegistrar() {
        return args -> {
            // 尝试获取 Nacos 配置处理器
            Object nacosRefresherHandler = null;
            try {
                nacosRefresherHandler = applicationContext.getBean("nacosCloudRefresherHandler");
            } catch (Exception e) {
                log.debug("未找到 NacosCloudRefresherHandler bean，可能未启用 Nacos");
            }

            // 如果存在 Nacos 配置处理器，等待配置加载
            if (nacosRefresherHandler != null) {
                int maxWaitSeconds = 30;
                int waitedSeconds = 0;

                // 调用 triggerInitialConfigLoad 方法
                try {
                    java.lang.reflect.Method triggerMethod = nacosRefresherHandler.getClass().getMethod("triggerInitialConfigLoad");
                    triggerMethod.invoke(nacosRefresherHandler);
                    log.info("已触发 Nacos 配置主动拉取");
                } catch (Exception e) {
                    log.debug("触发 Nacos 配置拉取失败", e);
                }

                // 等待配置加载
                while (waitedSeconds < maxWaitSeconds) {
                    try {
                        java.lang.reflect.Method isLoadedMethod = nacosRefresherHandler.getClass().getMethod("isNacosConfigLoaded");
                        Boolean isLoaded = (Boolean) isLoadedMethod.invoke(nacosRefresherHandler);
                        if (Boolean.TRUE.equals(isLoaded)) {
                            break;
                        }
                    } catch (Exception e) {
                        log.debug("检查 Nacos 配置加载状态失败", e);
                        break;
                    }

                    try {
                        TimeUnit.SECONDS.sleep(1);
                        waitedSeconds++;
                        if (waitedSeconds % 5 == 0) {
                            log.info("等待 Nacos 配置加载... 已等待 {} 秒", waitedSeconds);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (waitedSeconds < maxWaitSeconds) {
                    log.info("Nacos 配置已加载，等待 500 毫秒确保配置生效...");
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log.warn("等待 Nacos 配置超时 ({} 秒)，将使用当前配置注册线程池", maxWaitSeconds);
                }
            } else {
                log.info("未检测到 Nacos 配置，使用本地配置注册线程池");
            }

            log.info("开始注册动态线程池...");
            // 调用AOP切面注册所有收集到的线程池
            DynamicThreadPoolAspect.registerAllPendingThreadPools();

            log.info("动态线程池注册完成");
        };
    }
}
