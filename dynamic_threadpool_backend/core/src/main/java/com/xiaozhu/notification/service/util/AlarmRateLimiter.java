package com.xiaozhu.notification.service.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:23
 */
public class AlarmRateLimiter {
    /**
     * 报警记录缓存 key: threadPoolId + "|" + alarmType
     */
    private static final Map<String, Long> ALARM_RECORD = new ConcurrentHashMap<>();

    /**
     * 是否允许报警
     * @param threadPoolId
     * @param alarmType
     * @param intervalMinutes 报警间隔时间
     * @return
     */
    public static boolean allowAlarm(String threadPoolId, String alarmType, int intervalMinutes) {
        String key = buildKey(threadPoolId, alarmType);
        long currentTime = System.currentTimeMillis();
        // 使用 compute 方法来更新记录
        return ALARM_RECORD.compute(key, (k, lastTime) -> {
            // 如果没有记录或者记录时间超过指定间隔时间，则允许发送报警
            if (lastTime == null || (currentTime - lastTime) > intervalMinutes * 60 * 1000L) {
                return currentTime; // 更新时间为当前时间
            }
            return lastTime; // 保持原时间
        }) == currentTime; // 返回值等于当前时间说明允许发送
    }

    private static String buildKey(String threadPoolId, String alarmType) {
        return threadPoolId + "|" + alarmType;
    }
}
