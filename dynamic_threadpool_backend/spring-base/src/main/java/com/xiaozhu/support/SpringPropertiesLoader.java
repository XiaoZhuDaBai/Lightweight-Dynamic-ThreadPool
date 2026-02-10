package com.xiaozhu.support;

import com.xiaozhu.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

/**
 * Spring属性加载器
 * 负责加载Spring应用的基本属性到ApplicationProperties中
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/22 16:21
 */
@Slf4j
public class SpringPropertiesLoader implements InitializingBean {

    private final Environment environment;

    public SpringPropertiesLoader(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 使用Environment获取属性，确保在所有配置加载完成后执行
        String applicationName = environment.getProperty("spring.application.name", "UNKNOWN");
        String activeProfile = environment.getProperty("spring.profiles.active", "UNKNOWN");

        log.info("加载应用属性 - 应用名: {}, 环境: {}", applicationName, activeProfile);

        ApplicationProperties.setApplicationName(applicationName);
        ApplicationProperties.setActiveProfile(activeProfile);

        // 验证设置是否成功
        log.debug("ApplicationProperties设置完成 - 应用名: {}, 环境: {}",
                ApplicationProperties.getApplicationName(),
                ApplicationProperties.getActiveProfile());
    }
}
