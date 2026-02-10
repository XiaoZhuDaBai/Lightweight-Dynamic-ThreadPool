package com.xiaozhu.configuration;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.enable.MarkerConfiguration;
import com.xiaozhu.refresh.NacosCloudRefresherHandler;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/9/11 12:35
 */
@Configurable
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX, value = "enable", matchIfMissing = true, havingValue = "true")
public class NacosCloudConfiguration {

    @Bean
    public NacosCloudRefresherHandler nacosCloudRefresherHandler(NacosConfigManager nacosConfigManager, XiaozhuBootstrapConfigProperties properties) {
        return new NacosCloudRefresherHandler(nacosConfigManager.getConfigService(), properties);
    }
}
