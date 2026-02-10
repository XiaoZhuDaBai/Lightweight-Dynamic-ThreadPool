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
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class CommonAutoConfiguration {

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
     */
    @Bean
    public ApplicationRunner dynamicThreadPoolRegistrar() {
        return args -> {
            log.info("开始注册动态线程池...");
            // 调用AOP切面注册所有收集到的线程池
            DynamicThreadPoolAspect.registerAllPendingThreadPools();

            log.info("动态线程池注册完成");
        };
    }
}
