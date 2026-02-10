package com.xiaozhu.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 应用属性
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/18 16:00
 */
public class ApplicationProperties {
    /**
     * 应用名
     */
    @Getter
    @Setter
    private static String applicationName;

    /**
     * 环境标识
     */
    @Getter
    @Setter
    private static String activeProfile;

    /**
     * 服务端口
     */
    @Getter
    @Setter
    private static String serverPort;
}
