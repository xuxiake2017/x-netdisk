package group.xuxiake.web.util;

import group.xuxiake.web.configuration.CustomConfiguration;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * 注册邮箱验证码发送工具。
 */
public final class EmailSendUtil {

    private EmailSendUtil() {
    }

    public static void sendVerificationCode(String receiveMailAccount, String code,
                                            long expireMinutes) throws Exception {
        Session session = createSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(CustomConfiguration.getEmailAccount(), "X-NetDisk", "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMailAccount));
        message.setSubject("X-NetDisk注册验证码", "UTF-8");
        message.setContent(createVerificationCodeContent(code, expireMinutes), "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();

        Transport transport = session.getTransport();
        try {
            transport.connect(CustomConfiguration.getEmailAccount(), CustomConfiguration.getEmailPassword());
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            if (transport.isConnected()) {
                transport.close();
            }
        }
    }

    private static Session createSession() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", CustomConfiguration.getEmailSMTPHost());
        props.setProperty("mail.smtp.auth", "true");

        String smtpPort = CustomConfiguration.getSmtpPort();
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);
        props.setProperty("mail.smtp.connectiontimeout", "5000");
        props.setProperty("mail.smtp.timeout", "10000");
        props.setProperty("mail.smtp.writetimeout", "10000");
        return Session.getInstance(props);
    }

    private static String createVerificationCodeContent(String code, long expireMinutes) {
        return "<html><body><p>您好：</p><p>您的注册验证码为："
                + "<strong style=\"font-size:24px\">" + code + "</strong></p><p>验证码 "
                + expireMinutes + " 分钟内有效，请勿转发给他人。</p></body></html>";
    }
}
