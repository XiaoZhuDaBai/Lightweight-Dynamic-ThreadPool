package com.xiaozhu.configuration;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.xiaozhu.refresh.NacosCloudRefresherHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/9/11 12:35
 */
@Configuration
@ConditionalOnProperty(prefix = "xiaozhu", value = "dynamicPool", matchIfMissing = true, havingValue = "true")
public class NacosCloudConfiguration {

    @Bean
    public NacosCloudRefresherHandler nacosCloudRefresherHandler(NacosConfigManager nacosConfigManager) {
        return new NacosCloudRefresherHandler(nacosConfigManager.getConfigService());
    }
}
