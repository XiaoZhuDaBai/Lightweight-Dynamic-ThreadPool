package com.xiaozhu.notification.service;

import cn.hutool.core.collection.CollectionUtil;
import com.xiaozhu.config.BootstrapConfigProperties;
import com.xiaozhu.notification.dto.ThreadPoolAlarmNotifyDTO;
import com.xiaozhu.notification.dto.ThreadPoolConfigChangeDTO;
import com.xiaozhu.notification.service.util.EmailConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.text.html.parser.Parser;
import java.util.List;
import java.util.Map;

import static com.xiaozhu.text.text.EMAIL_ALARM_NOTIFY_MESSAGE_TEXT;
import static com.xiaozhu.text.text.EMAIL_CONFIG_CHANGE_MESSAGE_TEXT;

/**
 * 邮件发送通知消息
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:08
 */
@Setter
@Slf4j
public class EmailMessageServiceImpl implements NotifierService{
    private String account;
    private String auth;
    private String smtpHost = "smtp.qq.com";
    private Integer smtpPort = 465;

    @Override
    public void configure(Object config) {
        if (config instanceof BootstrapConfigProperties.EmailConfig emailConfig) {
            if (emailConfig.getAccount() != null) {
                this.account = emailConfig.getAccount();
            }
            if (emailConfig.getAuth() != null) {
                this.auth = emailConfig.getAuth();
            }
            if (emailConfig.getSmtpHost() != null) {
                this.smtpHost = emailConfig.getSmtpHost();
            }
            if (emailConfig.getSmtpPort() != null) {
                this.smtpPort = emailConfig.getSmtpPort();
            }
        }
    }

    @Override
    public void sendChangeMessage(ThreadPoolConfigChangeDTO configChangeDTO) {
        Map<String, ThreadPoolConfigChangeDTO.ChangePair<?>> changes = configChangeDTO.getChanges();
        String text = String.format(
                EMAIL_CONFIG_CHANGE_MESSAGE_TEXT,
                configChangeDTO.getActiveProfile().toUpperCase(),
                configChangeDTO.getThreadPoolId(),
                configChangeDTO.getIdentify() + ":" + configChangeDTO.getApplicationName(),
                changes.get("corePoolSize").getBefore() + " ➲ " + changes.get("corePoolSize").getAfter(),
                changes.get("maximumPoolSize").getBefore() + " ➲ " + changes.get("maximumPoolSize").getAfter(),
                changes.get("keepAliveTime").getBefore() + " ➲ " + changes.get("keepAliveTime").getAfter(),
                configChangeDTO.getWorkQueue(),
                changes.get("queueCapacity").getBefore() + " ➲ " + changes.get("queueCapacity").getAfter(),
                changes.get("rejectedHandler").getBefore(),
                changes.get("rejectedHandler").getAfter(),
                configChangeDTO.getReceives(),
                configChangeDTO.getUpdateTime()
        );
//        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        String recipient = configChangeDTO.getReceives();
        sendEmailMarkdownMessage("动态线程池通知", text, recipient);
    }

    @Override
    public void sendAlarmMessage(ThreadPoolAlarmNotifyDTO alarm) {
        String text = String.format(
                EMAIL_ALARM_NOTIFY_MESSAGE_TEXT,
                alarm.getActiveProfile().toUpperCase(),
                alarm.getThreadPoolId(),
                alarm.getIdentify() + ":" + alarm.getApplicationName(),
                alarm.getAlarmType(),
                alarm.getCorePoolSize(),
                alarm.getMaximumPoolSize(),
                alarm.getCurrentPoolSize(),
                alarm.getActivePoolSize(),
                alarm.getLargestPoolSize(),
                alarm.getCompletedTaskCount(),
                alarm.getWorkQueueName(),
                alarm.getWorkQueueCapacity(),
                alarm.getWorkQueueSize(),
                alarm.getWorkQueueRemainingCapacity(),
                alarm.getRejectedHandlerName(),
                alarm.getRejectCount(),
                alarm.getReceives(),
                alarm.getInterval(),
                alarm.getCurrentTime()
        );
//        List<String> atMobiles = CollectionUtil.newArrayList(configChangeDTO.getReceives().split(","));
        String recipient = alarm.getReceives();
        sendEmailMarkdownMessage("报警通知", text, recipient);
    }

    private void sendEmailMarkdownMessage(String title, String text, String recipient) {
        log.info("发送通知邮件");

        // 检查必要参数
        if (recipient == null || recipient.trim().isEmpty()) {
            log.warn("邮件接收人为空，跳过发送");
            return;
        }
        if (account == null || account.trim().isEmpty()) {
            log.warn("邮件账号未配置，跳过发送");
            return;
        }
        if (auth == null || auth.trim().isEmpty()) {
            log.warn("邮件授权码未配置，跳过发送");
            return;
        }

        Session session = EmailConfig.createEmailSession(account, auth, smtpHost, smtpPort);
        // 创建邮件
        MimeMessage message = new MimeMessage(session);

        try {
            message.setSubject(title);
            // 发送html格式的邮件
            message.setContent(text, "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(account));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient));

            Transport.send(message);
        } catch (MessagingException e) {
            log.error("邮件发送出错", e);
        }
    }
}
