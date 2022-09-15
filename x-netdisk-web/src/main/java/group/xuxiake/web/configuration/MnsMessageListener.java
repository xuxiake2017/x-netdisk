package group.xuxiake.web.configuration;

import com.alicom.mns.tools.MessageListener;
import com.aliyun.mns.model.Message;
import com.google.gson.Gson;
import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.enums.SmsLogSuccess;
import group.xuxiake.common.mapper.SmsLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("messageListener")
public class MnsMessageListener implements MessageListener {
    private Gson gson = new Gson();
    @Resource
    private SmsLogMapper smsLogMapper;
    @Override
    public boolean dealMessage(Message message) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String messageBody = message.getMessageBodyAsString();
        try {
            Map<String, Object> contentMap = gson.fromJson(messageBody, HashMap.class);
            String bizId = (String) contentMap.get("biz_id");
            String sendTime = (String) contentMap.get("send_time");
            String reportTime = (String) contentMap.get("report_time");
            boolean success = (boolean) contentMap.get("success");
            String errCode = (String) contentMap.get("err_code");
            String errMsg = (String) contentMap.get("err_msg");
            String phoneNumber = (String) contentMap.get("phone_number");
            SmsLog smsLog = smsLogMapper.selectByBizId(bizId);
            if (smsLog == null) {
                smsLog = new SmsLog();
                smsLog.setBizId(bizId);
                smsLog.setSendTime(format.parse(sendTime));
                smsLog.setReportTime(format.parse(reportTime));
                smsLog.setSuccess(success ? SmsLogSuccess.SUCCESS.getValue() : SmsLogSuccess.FAILED.getValue());
                smsLog.setErrCode(errCode);
                smsLog.setErrMsg(errMsg);
                smsLog.setPhoneNumber(phoneNumber);
                smsLog.setMnsMessageBody(messageBody);
                smsLogMapper.insertSelective(smsLog);
            } else {
                smsLog.setSendTime(format.parse(sendTime));
                smsLog.setReportTime(format.parse(reportTime));
                smsLog.setSuccess(success ? SmsLogSuccess.SUCCESS.getValue() : SmsLogSuccess.FAILED.getValue());
                smsLog.setErrCode(errCode);
                smsLog.setErrMsg(errMsg);
                smsLog.setMnsMessageBody(messageBody);
                smsLogMapper.updateByPrimaryKeySelective(smsLog);
            }
        } catch(com.google.gson.JsonSyntaxException e){
            log.error("error_json_format: " + message.getMessageBodyAsString(), e);
            // 理论上不会出现格式错误的情况，所以遇见格式错误的消息，只能先delete,否则重新推送也会一直报错
            return true;
        } catch (Throwable e) {
            // 您自己的代码部分导致的异常，应该return false,这样消息不会被delete掉，而会根据策略进行重推
            return false;
        }

        // 消息处理成功，返回true, SDK将调用MNS的delete方法将消息从队列中删除掉
        return true;
    }
}
