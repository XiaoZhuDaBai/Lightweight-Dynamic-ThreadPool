package com.xiaozhu.refresh;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.xiaozhu.builder.ThreadPoolExecutorBuilder;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.executor.myenum.BlockingQueueTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/9/11 12:27
 */
@Slf4j
public class NacosCloudRefresherHandler extends AbstractDynamicThreadPoolRefresher {
    private final ConfigService configService;

    public NacosCloudRefresherHandler(ConfigService configService, BootstrapConfigProperties properties) {
        super(properties);
        this.configService = configService;
    }
    @Override
    protected void registerListener() throws Exception {
        BootstrapConfigProperties.NacosConfig nacosConfig = properties.getNacos();
        if (nacosConfig == null) {
            log.warn("Nacos配置不存在，跳过注册监听器");
            return;
        }

        try {
            configService.addListener(
                    nacosConfig.getDataId(),
                    nacosConfig.getGroup(),
                    new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return null;
                        }
                        @Override
                    public void receiveConfigInfo(String configInfo) {
                        log.info("Xiaozhu Nacos监听器接收到配置变化, 配置文件内容长度: {}", configInfo.length());
                        log.debug("Xiaozhu Nacos配置内容: {}", configInfo);
                        refreshThreadPoolProperties(configInfo);
                    }
                    });
            log.info("动态线程池刷新器, 成功添加 nacos 配置文件. data-id: {}, group: {}", nacosConfig.getDataId(), nacosConfig.getGroup());
        } catch (Exception e) {
            log.error("注册Nacos监听器失败", e);
            throw e;
        }
    }
}
