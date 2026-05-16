package com.xiaozhu.refresh;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.xiaozhu.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Nacos 配置监听处理器
 * 监听 Nacos 配置中心中动态线程池配置的变更
 *
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/9/11 12:27
 */
@Slf4j
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {
    
    private final ConfigService configService;
    
    private volatile boolean listenerRegistered = false;

    /**
     * -- GETTER --
     *  检查 Nacos 配置是否已加载
     */
    @Getter
    private volatile boolean nacosConfigLoaded = false;

    public NacosCloudRefresherHandler(ConfigService configService) {
        super(BootstrapConfigProperties.getInstance());
        this.configService = configService;
    }

    /**
     * 主动触发首次配置拉取
     * 解决本地配置先于 Nacos 配置被加载的问题
     */
    public void triggerInitialConfigLoad() {
        log.info("主动触发 Nacos 配置首次拉取...");
        try {
            String[] nacosConfig = parseNacosConfigFromNacos();
            if (nacosConfig != null) {
                String dataId = nacosConfig[0];
                String group = nacosConfig[1];
                log.info("获取到 Nacos 配置信息: dataId={}, group={}", dataId, group);
                
                // 主动获取配置内容
                String content = configService.getConfig(dataId, group, 5000);
                if (content != null && !content.isEmpty()) {
                    log.info("首次拉取 Nacos 配置成功，内容长度: {}", content.length());
                    // 解析并更新配置
                    refreshThreadPoolProperties(content);
                    nacosConfigLoaded = true;
                    return;
                }
            }
            log.warn("未能获取到有效的 Nacos 配置");
        } catch (Exception e) {
            log.error("主动拉取 Nacos 配置失败", e);
        }
    }
    
    @Override
    protected void registerListener() throws Exception {
        // 方案1: 从 BootstrapConfigProperties 获取（如果本地配置了 xiaozhu.nacos）
        BootstrapConfigProperties bootstrapConfig = BootstrapConfigProperties.getInstance();
        String dataId = null;
        String group = "DEFAULT_GROUP";
        
        if (bootstrapConfig != null && bootstrapConfig.getNacos() != null) {
            dataId = bootstrapConfig.getNacos().getDataId();
            group = bootstrapConfig.getNacos().getGroup();
            log.info("从BootstrapConfigProperties获取Nacos配置: dataId={}, group={}", dataId, group);
        }
        
        // 方案2: 如果方案1失败，尝试从 Nacos 配置文件中解析
        if (dataId == null || dataId.isEmpty()) {
            log.info("从BootstrapConfigProperties获取Nacos配置失败，尝试从Nacos配置文件解析...");
            String[] nacosConfig = parseNacosConfigFromNacos();
            if (nacosConfig != null) {
                dataId = nacosConfig[0];
                group = nacosConfig[1];
                log.info("从Nacos配置文件解析成功: dataId={}, group={}", dataId, group);
            }
        }
        
        if (dataId == null || dataId.isEmpty()) {
            log.warn("未找到xiaozhu.nacos配置，跳过注册监听器");
            return;
        }
        
        doRegisterListener(dataId, group);
    }
    
    /**
     * 从 Nacos 共享配置文件中解析 xiaozhu.nacos 配置
     * 
     * 这个方法会尝试从多个常见的 Nacos 配置文件 dataId 中获取 xiaozhu 配置，
     * 然后解析其中的 nacos.dataId 和 nacos.group
     */
    private String[] parseNacosConfigFromNacos() {
        // 常见的 Nacos 配置文件 dataId 列表
        // 可以根据实际情况扩展这个列表
        List<String> candidateDataIds = List.of(
                "dynamic-threadpool-demo.yaml",
                "dynamic-threadpool.yaml",
                "xiaozhu-threadpool.yaml",
                "application.yaml",
                "application.yml"
        );
        
        for (String dataId : candidateDataIds) {
            try {
                String content = configService.getConfig(dataId, "DEFAULT_GROUP", 3000);
                if (content != null && !content.isEmpty()) {
                    String[] nacosConfig = extractNacosConfig(content, dataId);
                    if (nacosConfig != null) {
                        return nacosConfig;
                    }
                }
            } catch (Exception e) {
                log.debug("尝试从 {} 获取配置失败", dataId, e);
            }
            
            // 尝试不同的 group
            for (String group : List.of("DEFAULT_GROUP", "SEVER_DEV", "DEV", "TEST")) {
                try {
                    String content = configService.getConfig(dataId, group, 3000);
                    if (content != null && !content.isEmpty()) {
                        String[] nacosConfig = extractNacosConfig(content, dataId);
                        if (nacosConfig != null) {
                            nacosConfig[1] = group; // 使用找到的 group
                            return nacosConfig;
                        }
                    }
                } catch (Exception e) {
                    log.debug("尝试从 {}+{} 获取配置失败", dataId, group, e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 从配置内容中提取 xiaozhu.nacos 配置
     */
    @SuppressWarnings("unchecked")
    private String[] extractNacosConfig(String content, String dataId) {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(content);
            if (config == null) {
                return null;
            }
            
            Object xiaozhu = config.get("xiaozhu");
            if (xiaozhu instanceof Map) {
                Map<String, Object> xiaozhuMap = (Map<String, Object>) xiaozhu;
                Object nacos = xiaozhuMap.get("nacos");
                if (nacos instanceof Map) {
                    Map<String, Object> nacosMap = (Map<String, Object>) nacos;
                    Object dataIdValue = nacosMap.get("dataId");
                    if (dataIdValue != null && !dataIdValue.toString().isEmpty()) {
                        String foundDataId = dataIdValue.toString();
                        String foundGroup = "DEFAULT_GROUP";
                        Object groupValue = nacosMap.get("group");
                        if (groupValue != null) {
                            foundGroup = groupValue.toString();
                        }
                        return new String[] { foundDataId, foundGroup };
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析配置内容失败: {}", dataId, e);
        }
        return null;
    }
    
    /**
     * 执行监听器注册
     */
    private void doRegisterListener(String dataId, String group) {
        try {
            configService.addListener(
                    dataId,
                    group,
                    new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return null;
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            log.info("Xiaozhu Nacos监听器接收到配置变化, 配置文件内容长度: {}", configInfo.length());
                            log.debug("Xiaozhu Nacos配置内容: {}", configInfo);
                            nacosConfigLoaded = true;
                            refreshThreadPoolProperties(configInfo);
                        }
                    });
            listenerRegistered = true;
            nacosConfigLoaded = true;
            log.info("动态线程池刷新器, 成功添加 nacos 配置文件. data-id: {}, group: {}", dataId, group);
        } catch (Exception e) {
            log.error("注册Nacos监听器失败", e);
            listenerRegistered = false;
        }
    }
}
