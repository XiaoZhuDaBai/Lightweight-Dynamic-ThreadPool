package com.xiaozhu.notification.service;

import com.xiaozhu.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.xiaozhu.notification.dto.ThreadPoolConfigChangeDTO;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:02
 */
public interface NotifierService {

    /**
     * 获取通知服务类型标识
     * @return 服务类型，如 "EMAIL", "WECHAT", "DINGTALK"
     */
    default String getType() {
        return this.getClass().getSimpleName().replace("MessageServiceImpl", "").toUpperCase();
    }

    /**
     * 配置通知服务
     * @param config 配置信息
     */
    default void configure(Object config) {
        // 默认空实现，由具体服务类覆盖
    }

    /**
     * 发送线程池配置变更通知
     *
     * @param configChange 配置变更信息
     */
    void sendChangeMessage(ThreadPoolConfigChangeDTO configChange);

    /**
     * 发送 Web 线程池配置变更通知
     *
     * @param configChange 配置变更信息
     */
//    void sendWebChangeMessage(WebThreadPoolConfigChangeDTO configChange);

    /**
     * 发送线程池报警通知
     *
     * @param alarm 报警信息
     */
    void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm);
}
