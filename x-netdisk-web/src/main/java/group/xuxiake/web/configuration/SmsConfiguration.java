package group.xuxiake.web.configuration;

import group.xuxiake.common.entity.SmsTemplate;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties("app.config.sms")
@Data
public class SmsConfiguration {

    private List<SmsTemplate> templateCodeList;

    public String getTemplateContent(String templateCode) {
        if (templateCodeList == null || templateCodeList.size() == 0) {
            return "";
        } else {
            List<SmsTemplate> result = templateCodeList.stream().filter(smsTemplate -> smsTemplate.getTemplateCode().equals(templateCode)).collect(Collectors.toList());
            if (result.size() > 0) {
                return result.get(0).getTemplateContent();
            }
        }
        return "";
    }
}
