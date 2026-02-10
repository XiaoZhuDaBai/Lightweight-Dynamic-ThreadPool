package com.xiaozhu.refresh;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/21 16:09
 */

import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.parser.ConfigFileTypeEnum;
import com.xiaozhu.parser.ConfigParserHandler;
import com.xiaozhu.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

/**
 *  模板方法设计刷新器
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDynamicThreadPoolRefresher implements ApplicationRunner {

    protected final BootstrapConfigProperties properties;

    /**
     * 注册配置变更监听器，由子类实现具体逻辑
     *
     * @throws Exception
     */
    protected abstract void registerListener() throws Exception;

    /**
     * 默认空实现，子类可以按需覆盖
     */
    protected void beforeRegister() {
    }

    /**
     * 默认空实现，子类可以按需覆盖
     */
    protected void afterRegister() {
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        beforeRegister();
        registerListener();
        afterRegister();
    }

    @SneakyThrows
    public void refreshThreadPoolProperties(String configInfo) {
        log.info("开始解析Xiaozhu线程池配置...");

        // 如果configFileType为null，尝试推断文件类型
        ConfigFileTypeEnum fileType = properties.getConfigFileType();
        if (fileType == null) {
            fileType = inferConfigFileType(configInfo);
            log.info("配置文件类型未设置，推断为: {}", fileType);
        }

        Map<Object, Object> configInfoMap = ConfigParserHandler.getInstance().parseConfig(configInfo, fileType);
        log.debug("配置解析结果: {}", configInfoMap);

        ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
        Binder binder = new Binder(sources);
        BootstrapConfigProperties refresherProperties = binder.bind(BootstrapConfigProperties.PREFIX, Bindable.of(BootstrapConfigProperties.class)).get();

        log.info("配置绑定完成, 线程池数量: {}", refresherProperties.getExecutors() != null ? refresherProperties.getExecutors().size() : 0);
        if (refresherProperties.getExecutors() != null) {
            for (var executor : refresherProperties.getExecutors()) {
                log.info("检测到线程池配置: {} (核心线程数: {}, 最大线程数: {})",
                        executor.getThreadPoolId(), executor.getCorePoolSize(), executor.getMaximumPoolSize());
            }
        } else {
            log.warn("配置中没有找到线程池配置 (executors 字段为空或不存在)");
        }

        // 发布配置文件观察变更事件，通知观察者们进行线程池检查和变更
        log.info("发布线程池配置更新事件...");
        ApplicationContextHolder.publishEvent(new ThreadPoolConfigUpdateEvent(this, refresherProperties));
        log.info("线程池配置更新事件发布完成");
    }

    /**
     * 推断配置文件类型
     * @param configInfo 配置文件内容
     * @return 配置文件类型
     */
    private ConfigFileTypeEnum inferConfigFileType(String configInfo) {
        if (configInfo == null || configInfo.trim().isEmpty()) {
            return ConfigFileTypeEnum.PROPERTIES;
        }

        String trimmed = configInfo.trim();

        // 检查是否为YAML格式（以---开头或者包含:语法）
        if (trimmed.startsWith("---") ||
            trimmed.contains(": ") ||
            trimmed.contains(":\n") ||
            trimmed.contains(":\r\n")) {
            return ConfigFileTypeEnum.YAML;
        }

        // 检查是否为Properties格式（包含=号）
        if (trimmed.contains("=") && !trimmed.contains(":")) {
            return ConfigFileTypeEnum.PROPERTIES;
        }

        // 默认返回YAML，因为我们的配置文件是YAML格式的
        return ConfigFileTypeEnum.YAML;
    }
}
