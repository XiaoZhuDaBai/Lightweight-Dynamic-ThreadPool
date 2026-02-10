package com.xiaozhu.configuration;

import com.xiaozhu.config.BootstrapConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

/**
 * Xiaozhu 配置属性 Spring 适配器
 *
 * <p>职责：继承核心配置类，添加 Spring 配置绑定支持</p>
 * <p>这样 core 模块无需依赖 Spring，保持核心模块的独立性</p>
 *
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/2/7
 */
@ConfigurationProperties(prefix = "xiaozhu")
@Primary
public class XiaozhuBootstrapConfigProperties extends BootstrapConfigProperties {

}
