package com.xiaozhu.text;

/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/17 17:24
 */
public class text {
    /**
     * 线程池参数变更日志打印常量
     */
    public static final String CHANGE_THREAD_POOL_TEXT = """
            [{}] Dynamic thread pool parameter changed:
                corePoolSize: {}
                maximumPoolSize: {}
                capacity: {}
                keepAliveTime: {}
                rejectedType: {}
                allowCoreThreadTimeOut: {}""";

    /**
     * 线程池参数变更前后分隔符常量
     */
    public static final String CHANGE_DELIMITER = "%s => %s";

    /**
     * 邮件配置变更消息文本 (HTML格式)
     */
    public static final String EMAIL_CONFIG_CHANGE_MESSAGE_TEXT = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2a9d8f; border-bottom: 2px solid #2a9d8f; padding-bottom: 10px;">
                    📢 [通知] %s - 动态线程池参数变更
                </h2>

                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold; width: 140px;">线程池ID：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">应用实例：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">核心线程数：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">最大线程数：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">线程存活时间：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">队列类型：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">队列容量：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">旧拒绝策略：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">新拒绝策略：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">接收人：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                    </table>
                </div>

                <div style="background-color: #e8f4fd; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #2a9d8f;">
                    <p style="margin: 0; color: #666; font-size: 14px;">
                        💡 <strong>提示：</strong>动态线程池配置变更实时通知（无限制）
                    </p>
                </div>

                <div style="text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;">
                    变更时间：%s
                </div>
            </div>
            """;

    /**
     * 邮件Web线程池变更消息文本 (HTML格式)
     */
    public static final String EMAIL_CONFIG_WEB_CHANGE_MESSAGE_TEXT = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2a9d8f; border-bottom: 2px solid #2a9d8f; padding-bottom: 10px;">
                    📢 [通知] %s - %s线程池参数变更
                </h2>

                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold; width: 140px;">应用实例：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">核心线程数：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">最大线程数：</td>
                            <td style="padding: 8px 0; color: #333; font-weight: bold; color: #2a9d8f;">%s</td>
                        </tr>
                        <tr style="background-color: #fff;">
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">线程存活时间：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666; font-weight: bold;">接收人：</td>
                            <td style="padding: 8px 0; color: #333;">%s</td>
                        </tr>
                    </table>
                </div>

                <div style="background-color: #e8f4fd; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #2a9d8f;">
                    <p style="margin: 0; color: #666; font-size: 14px;">
                        💡 <strong>提示：</strong>%s线程池配置变更实时通知（无限制）
                    </p>
                </div>

                <div style="text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;">
                    变更时间：%s
                </div>
            </div>
            """;

    /**
     * 邮件报警消息文本 (HTML格式)
     */
    public static final String EMAIL_ALARM_NOTIFY_MESSAGE_TEXT = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #dc3545; border-bottom: 2px solid #dc3545; padding-bottom: 10px;">
                    🚨 [警报] %s - 动态线程池运行告警
                </h2>

                <div style="background-color: #f8d7da; border: 1px solid #f5c6cb; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <table style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 8px 0; color: #721c24; font-weight: bold; width: 140px;">线程池ID：</td>
                            <td style="padding: 8px 0; color: #721c24; font-weight: bold;">%s</td>
                        </tr>
                        <tr style="background-color: #fff5f5;">
                            <td style="padding: 8px 0; color: #721c24; font-weight: bold;">应用实例：</td>
                            <td style="padding: 8px 0; color: #721c24;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #721c24; font-weight: bold;">告警类型：</td>
                            <td style="padding: 8px 0; color: #dc3545; font-weight: bold; font-size: 16px;">%s</td>
                        </tr>
                    </table>
                </div>

                <div style="display: flex; gap: 20px; margin: 20px 0;">
                    <div style="flex: 1; background-color: #f8f9fa; padding: 15px; border-radius: 6px;">
                        <h3 style="color: #495057; margin: 0 0 10px 0; font-size: 16px;">📊 线程状态</h3>
                        <table style="width: 100%%; border-collapse: collapse; font-size: 13px;">
                            <tr>
                                <td style="padding: 4px 0; color: #666;">核心线程数：</td>
                                <td style="padding: 4px 0; color: #333; font-weight: bold;">%d</td>
                            </tr>
                            <tr style="background-color: #fff;">
                                <td style="padding: 4px 0; color: #666;">最大线程数：</td>
                                <td style="padding: 4px 0; color: #333; font-weight: bold;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 4px 0; color: #666;">当前线程数：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                            <tr style="background-color: #fff;">
                                <td style="padding: 4px 0; color: #666;">活跃线程数：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 4px 0; color: #666;">历史最大线程数：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                            <tr style="background-color: #fff;">
                                <td style="padding: 4px 0; color: #666;">任务总量：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                        </table>
                    </div>

                    <div style="flex: 1; background-color: #f8f9fa; padding: 15px; border-radius: 6px;">
                        <h3 style="color: #495057; margin: 0 0 10px 0; font-size: 16px;">📋 队列状态</h3>
                        <table style="width: 100%%; border-collapse: collapse; font-size: 13px;">
                            <tr>
                                <td style="padding: 4px 0; color: #666;">队列类型：</td>
                                <td style="padding: 4px 0; color: #333;">%s</td>
                            </tr>
                            <tr style="background-color: #fff;">
                                <td style="padding: 4px 0; color: #666;">队列容量：</td>
                                <td style="padding: 4px 0; color: #333; font-weight: bold;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 4px 0; color: #666;">当前元素数：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                            <tr style="background-color: #fff;">
                                <td style="padding: 4px 0; color: #666;">剩余容量：</td>
                                <td style="padding: 4px 0; color: #333;">%d</td>
                            </tr>
                        </table>
                    </div>
                </div>

                <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 6px; margin: 20px 0;">
                    <h3 style="color: #856404; margin: 0 0 10px 0; font-size: 16px;">⚠️ 拒绝策略统计</h3>
                    <table style="width: 100%%; border-collapse: collapse; font-size: 13px;">
                        <tr>
                            <td style="padding: 4px 0; color: #856404;">拒绝策略：</td>
                            <td style="padding: 4px 0; color: #856404;">%s</td>
                        </tr>
                        <tr style="background-color: #fffbf0;">
                            <td style="padding: 4px 0; color: #dc3545; font-weight: bold;">执行次数：</td>
                            <td style="padding: 4px 0; color: #dc3545; font-weight: bold; font-size: 16px;">%d 次</td>
                        </tr>
                    </table>
                </div>

                <div style="background-color: #e8f4fd; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #2a9d8f;">
                    <p style="margin: 0 0 8px 0; color: #666; font-size: 14px;">
                        👤 <strong>接收人：</strong>%s
                    </p>
                    <p style="margin: 0; color: #666; font-size: 14px;">
                        ⏰ <strong>提示：</strong>%d分钟内此线程池不会重复告警（可配置）
                    </p>
                </div>

                <div style="text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px;">
                    告警时间：%s
                </div>
            </div>
            """;
}
