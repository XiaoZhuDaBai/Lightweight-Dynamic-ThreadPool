package com.xiaozhu.enable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/21 16:28
 */
@Configuration
public class MarkerConfiguration {
    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    /**
     * 标记类
     * 可用于条件装配（@ConditionalOnBean 等）中作为存在性的判断依据
     */
    public static class Marker {

    }
}
