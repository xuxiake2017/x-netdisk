package group.xuxiake.web.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Author by xuxiake, Date on 2020/3/2 13:27.
 * PS: Not easy to write code, please indicate.
 * Description：
 */
@Data
@Component
public class CustomConfiguration {

    @Value("${app.config.totalMemory}")
    private Long totalMemory;
    @Value("${app.config.fileSizeMax}")
    private Long fileSizeMax;
    @Value("${app.config.vipFileSizeMax}")
    private Long vipFileSizeMax;
    private static String serverHost;
    @Value("${app.config.recycleDelayTime}")
    private Integer recycleDelayTime;


    private static Long verifyEmailExpire;
    private static String emailAccount;
    private static String emailPassword;
    private static String emailSMTPHost;
    private static String smtpPort;


    private static Integer verifySmsExpire;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String signName;
    private static String templateCode;
    private static String queueName;

    @Value("${app.config.tuling.tuling_apiKey}")
    private String tulingApiKey;
    @Value("${app.config.tuling.tuling_apiUrl}")
    private String tulingApiUrl;

    @Value("${app.config.wechat.appID}")
    private String wechatAppID;
    @Value("${app.config.wechat.appSecret}")
    private String wechatAppSecret;

    public static String getServerHost() {
        return serverHost;
    }

    @Value("${app.config.server_host}")
    public void setServerHost(String serverHost) {
        CustomConfiguration.serverHost = serverHost;
    }

    public static Long getVerifyEmailExpire() {
        return verifyEmailExpire;
    }

    @Value("${app.config.email.verifyEmailExpire}")
    public void setVerifyEmailExpire(Long verifyEmailExpire) {
        CustomConfiguration.verifyEmailExpire = verifyEmailExpire;
    }

    public static String getEmailAccount() {
        return emailAccount;
    }

    @Value("${app.config.email.emailAccount}")
    public void setEmailAccount(String emailAccount) {
        CustomConfiguration.emailAccount = emailAccount;
    }

    public static String getEmailPassword() {
        return emailPassword;
    }

    @Value("${app.config.email.emailPassword}")
    public void setEmailPassword(String emailPassword) {
        CustomConfiguration.emailPassword = emailPassword;
    }

    public static String getEmailSMTPHost() {
        return emailSMTPHost;
    }

    @Value("${app.config.email.emailSMTPHost}")
    public void setEmailSMTPHost(String emailSMTPHost) {
        CustomConfiguration.emailSMTPHost = emailSMTPHost;
    }

    public static String getSmtpPort() {
        return smtpPort;
    }

    @Value("${app.config.email.smtpPort}")
    public void setSmtpPort(String smtpPort) {
        CustomConfiguration.smtpPort = smtpPort;
    }

    public static Integer getVerifySmsExpire() {
        return verifySmsExpire;
    }

    @Value("${app.config.sms.verifySmsExpire}")
    public void setVerifySmsExpire(Integer verifySmsExpire) {
        CustomConfiguration.verifySmsExpire = verifySmsExpire;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    @Value("${app.config.sms.accessKeyId}")
    public void setAccessKeyId(String accessKeyId) {
        CustomConfiguration.accessKeyId = accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }

    @Value("${app.config.sms.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        CustomConfiguration.accessKeySecret = accessKeySecret;
    }

    public static String getSignName() {
        return signName;
    }

    @Value("${app.config.sms.signName}")
    public void setSignName(String signName) {
        CustomConfiguration.signName = signName;
    }

    public static String getTemplateCode() {
        return templateCode;
    }

    @Value("${app.config.sms.templateCode}")
    public void setTemplateCode(String templateCode) {
        CustomConfiguration.templateCode = templateCode;
    }

    public static String getQueueName() {
        return queueName;
    }

    @Value("${app.config.sms.queueName}")
    public void setQueueName(String queueName) {
        CustomConfiguration.queueName = queueName;
    }
}
