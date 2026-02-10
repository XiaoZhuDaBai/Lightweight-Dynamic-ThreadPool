package com.xiaozhu.config;

import com.xiaozhu.executor.ThreadPoolExecutorProperties;
import com.xiaozhu.parser.ConfigFileTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/18 16:02
 */
@Data
public class BootstrapConfigProperties {
    public static final String PREFIX = "xiaozhu";
    /**
     * 是否开启动态线程池开关
     */
    private Boolean dynamicPool = Boolean.TRUE;
    /**
     * Nacos 配置文件
     */
    private NacosConfig nacos;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NacosConfig {
        private String dataId;
        private String group;
    }
    /**
     * Apollo 配置文件
     */
    private ApolloConfig apollo;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApolloConfig {
        private String namespace;
    }

    /**
     * Web 线程池配置
     */
    private WebThreadPoolExecutorConfig web;
    @Data
    public static class WebThreadPoolExecutorConfig {

        /**
         * 核心线程数
         */
        private Integer corePoolSize;

        /**
         * 最大线程数
         */
        private Integer maximumPoolSize;

        /**
         * 线程空闲存活时间（单位：秒）
         */
        private Long keepAliveTime;

        /**
         * 通知配置
         */
        private NotifyConfig notify;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotifyConfig {
        /**
         * 接收人集合
         */
        private String receives;
    }
    /**
     * Nacos 远程配置文件格式类型
     */
    private ConfigFileTypeEnum configFileType = ConfigFileTypeEnum.YAML;
    /**
     * 通知配置
     */
    private NotifyPlatformsConfig notifyPlatforms;
    @Data
    public static class NotifyPlatformsConfig {

        /**
         * 通知类型，比如：EMAIL, WECHAT, DINGTALK
         */
        private String platform;

        /**
         * 完整 WebHook 地址
         */
        private String url;

        /**
         * 邮件服务器配置
         */
        private EmailConfig email;

        /**
         * 微信配置
         */
        private WechatConfig wechat;

        /**
         * 钉钉配置
         */
        private DingTalkConfig dingTalk;
    }

    @Data
    public static class EmailConfig {
        /**
         * 邮件账号
         */
        private String account;

        /**
         * 邮件授权码/密码
         */
        private String auth;

        /**
         * 默认接收者邮箱，多个用逗号分隔
         */
        private String receivers;

        /**
         * SMTP服务器地址，默认QQ邮箱
         */
        private String smtpHost = "smtp.qq.com";

        /**
         * SMTP端口，默认465
         */
        private Integer smtpPort = 465;
    }

    @Data
    public static class WechatConfig {
        /**
         * 企业微信CorpID
         */
        private String corpId;

        /**
         * 企业微信应用Secret
         */
        private String corpSecret;

        /**
         * 企业微信应用AgentId
         */
        private Integer agentId;

        /**
         * 接收消息的用户ID列表，多个用逗号分隔
         */
        private String toUsers;
    }

    @Data
    public static class DingTalkConfig {
        /**
         * 钉钉机器人Webhook地址
         */
        private String webhookUrl;

        /**
         * 钉钉机器人访问令牌
         */
        private String accessToken;

        /**
         * 密钥，用于生成签名
         */
        private String secret;
    }

    /**
     * 监控配置
     */
    private MonitorConfig monitorConfig = new MonitorConfig();
    @Data
    public static class MonitorConfig {

        /**
         * 默认开启监控配置
         */
        private Boolean enable = Boolean.TRUE;

        /**
         * 监控类型
         */
        private String collectType = "micrometer";

        /**
         * 采集间隔，默认 10 秒
         */
        private Long collectInterval = 10L;
    }
    /**
     * 线程池配置集合
     */
    private List<ThreadPoolExecutorProperties> executors;


    // 单例模式
    private static BootstrapConfigProperties INSTANCE = new BootstrapConfigProperties();

    public static BootstrapConfigProperties getInstance() {
        return INSTANCE;
    }

    public static void setInstance(BootstrapConfigProperties properties) {
        INSTANCE = properties;
    }
}
