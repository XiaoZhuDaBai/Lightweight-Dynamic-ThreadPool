package com.xiaozhu.configuration;

import com.xiaozhu.alarm.ThreadPoolAlarmChecker;
import com.xiaozhu.monitor.ThreadPoolMonitor;
import com.xiaozhu.notification.service.NotifierDispatcher;
import com.xiaozhu.support.ApplicationContextHolder;
import com.xiaozhu.support.SpringPropertiesLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:25
 */
@Slf4j
@Configuration
public class XiaozhuBaseConfiguration {
    @Bean
    public NotifierDispatcher notifierDispatcher() {
        // 延迟初始化，等待所有配置加载完成
        // 初始化将在应用启动完成后通过事件监听器完成
        return new NotifierDispatcher();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolAlarmChecker threadPoolAlarmChecker(NotifierDispatcher notifierDispatcher) {
        return new ThreadPoolAlarmChecker(notifierDispatcher);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ThreadPoolMonitor threadPoolMonitor() {
        return new ThreadPoolMonitor();
    }

    @Bean
    public SpringPropertiesLoader springPropertiesLoader(Environment environment) {
        return new SpringPropertiesLoader(environment);
    }


    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    public ApplicationRunner notifierInitializer(NotifierDispatcher notifierDispatcher) {
        return args -> {
            log.info("初始化通知服务配置...");
            try {
                notifierDispatcher.initializeServices();
                log.info("通知服务配置初始化完成");
            } catch (Exception e) {
                log.error("通知服务配置初始化失败", e);
            }
        };
    }
}
