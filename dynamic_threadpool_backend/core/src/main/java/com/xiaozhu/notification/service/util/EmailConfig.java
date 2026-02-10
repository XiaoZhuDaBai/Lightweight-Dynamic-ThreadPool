package com.xiaozhu.notification.service.util;

import javax.mail.Session;
import java.util.Properties;

/**
 * 配置邮件信息
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/8/19 17:23
 */
public class EmailConfig {
    public static Session createEmailSeesion(String account, String auth) {
        return createEmailSession(account, auth, "smtp.qq.com", 465);
    }

    public static Session createEmailSession(String account, String auth, String smtpHost, Integer smtpPort) {
        //	创建一个配置文件，并保存
        Properties props = new Properties();

        //	SMTP服务器连接信息
        //  126——smtp.126.com
        //  163——smtp.163.com
        //  qq——smtp.qq.com"
        props.put("mail.smtp.host", smtpHost);//	SMTP主机名

        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true"); // 启用SSL加密
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // 强制使用SSL

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(account, auth);
            }
        });

        //  控制台打印调试信息
//        session.setDebug(true);
        session.setDebug(false);
        return session;
    }
}
