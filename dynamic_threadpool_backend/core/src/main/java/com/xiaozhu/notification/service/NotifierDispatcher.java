package com.xiaozhu.notification.service;

import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.xiaozhu.notification.dto.ThreadPoolConfigChangeDTO;
import com.xiaozhu.notification.service.util.AlarmRateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知类型控制器
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:04
 */
@Slf4j
public class NotifierDispatcher implements NotifierService{
    private static final Map<String, Class<? extends NotifierService>> NOTIFIER_SERVICE_CLASSES = new HashMap<>();
    private final Map<String, NotifierService> notifierInstances = new HashMap<>();

    // 注册可用的通知服务实现类
    static {
        NOTIFIER_SERVICE_CLASSES.put("EMAIL", EmailMessageServiceImpl.class);
        // 可以在这里注册其他通知服务，如：
        // NOTIFIER_SERVICE_CLASSES.put("WECHAT", WechatMessageServiceImpl.class);
        // NOTIFIER_SERVICE_CLASSES.put("DINGTALK", DingTalkMessageServiceImpl.class);
    }

    /**
     * 注册新的通知服务实现类
     * @param type 服务类型标识
     * @param serviceClass 服务实现类
     */
    public static void registerNotifierService(String type, Class<? extends NotifierService> serviceClass) {
        NOTIFIER_SERVICE_CLASSES.put(type.toUpperCase(), serviceClass);
    }

    /**
     * 初始化和配置通知服务
     */
    public void initializeServices() {
        BootstrapConfigProperties instance = BootstrapConfigProperties.getInstance();
        if (instance == null) {
            log.error("BootstrapConfigProperties instance is null, cannot initialize notification services");
            return;
        }

        BootstrapConfigProperties.NotifyPlatformsConfig notifyConfig = instance.getNotifyPlatforms();
        log.debug("Initializing notification services with config: {}", notifyConfig);

        if (notifyConfig != null) {
            // 配置邮件服务
            if (notifyConfig.getEmail() != null) {
                NotifierService emailService = getOrCreateService("EMAIL");
                if (emailService != null) {
                    emailService.configure(notifyConfig.getEmail());
                    log.info("Email notification service configured successfully");
                } else {
                    log.error("Failed to create email notification service");
                }
            }

            // 配置微信服务
            if (notifyConfig.getWechat() != null) {
                NotifierService wechatService = getOrCreateService("WECHAT");
                if (wechatService != null) {
                    wechatService.configure(notifyConfig.getWechat());
                    log.info("WeChat notification service configured successfully");
                }
            }

            // 配置钉钉服务
            if (notifyConfig.getDingTalk() != null) {
                NotifierService dingTalkService = getOrCreateService("DINGTALK");
                if (dingTalkService != null) {
                    dingTalkService.configure(notifyConfig.getDingTalk());
                    log.info("DingTalk notification service configured successfully");
                }
            }
        } else {
            log.warn("NotifyPlatformsConfig is null, no notification services will be configured");
        }
    }

    /**
     * 获取或创建通知服务实例
     */
    private NotifierService getOrCreateService(String type) {
        return notifierInstances.computeIfAbsent(type, key -> {
            Class<? extends NotifierService> serviceClass = NOTIFIER_SERVICE_CLASSES.get(key);
            if (serviceClass != null) {
                try {
                    return serviceClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create notifier service: " + key, e);
                }
            }
            return null;
        });
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChange) {
        Optional<NotifierService> notifierService = Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(this::getOrCreateService);
        notifierService.ifPresent(service -> service.sendChangeMessage(configChange));
    }

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        Optional<NotifierService> notifierService = Optional.ofNullable(BootstrapConfigProperties.getInstance().getNotifyPlatforms())
                .map(BootstrapConfigProperties.NotifyPlatformsConfig::getPlatform)
                .map(this::getOrCreateService);
        if (notifierService.isPresent()) {
            // 频率检查
            boolean allowSend = AlarmRateLimiter.allowAlarm(
                    alarm.getThreadPoolId(),
                    alarm.getAlarmType(),
                    alarm.getInterval()
            );

            // 满足频率发送告警
            if (allowSend) {
                notifierService.get().sendAlarmMessage(alarm);
            }
        }
    }
}
