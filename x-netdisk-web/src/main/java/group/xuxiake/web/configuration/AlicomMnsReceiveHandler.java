package group.xuxiake.web.configuration;

import com.alicom.mns.tools.DefaultAlicomMessagePuller;
import com.aliyuncs.exceptions.ClientException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Data
@Component
@Slf4j
public class AlicomMnsReceiveHandler {

    @Resource
    private MnsMessageListener messageListener;

    public void start() {
        try {
            DefaultAlicomMessagePuller puller = new DefaultAlicomMessagePuller();
            // 设置异步线程池大小及任务队列的大小，还有无数据线程休眠时间
            puller.setConsumeMinThreadSize(6);
            puller.setConsumeMaxThreadSize(16);
            puller.setThreadQueueSize(200);
            puller.setPullMsgThreadSize(1);
            //和服务端联调问题时开启,平时无需开启，消耗性能
            puller.openDebugLog(false);

            // TODO 此处需要替换成开发者自己的AK信息
            String accessKeyId = CustomConfiguration.getAccessKeyId();
            String accessKeySecret = CustomConfiguration.getAccessKeySecret();

            /*
             * TODO 将messageType和queueName替换成您需要的消息类型名称和对应的队列名称
             * 云通信产品下所有的回执消息类型:
             * 短信服务
             * 1:短信回执：SmsReport，
             * 2:短息上行：SmsUp
             * 3:国际短信回执：GlobeSmsReport
             *
             * 号码隐私保护服务
             * 1.呼叫发起时话单报告：SecretStartReport
             * 2.呼叫响铃时报告：SecretRingReport
             * 3.呼叫接听时报告：SecretPickUpReport
             * 4.呼叫结束后话单报告：SecretReport
             * 5.录音状态报告：SecretRecording
             * 6.录音ASR状态报告：SecretAsrReport
             * 7.短信内容报告：SecretSmsIntercept
             * 8.计费通话报告：SecretBillingCallReport
             * 9.计费短信报告：SecretBillingSmsReport
             * 10.异常号码状态推送：SecretExceptionPhoneReport
             * 11.放音录音状态报告：SecretRingRecording
             * 12.电商物流详情报告：SmartLogisticsReport
             * 13.号码管理信息：NumberManagementReport
             *
             * 语音服务
             * 1.呼叫记录消息：VoiceReport
             * 2.呼叫中间状态消息：VoiceCallReport
             * 3.录音记录消息：VoiceRecordReport
             * 4.实时ASR消息：VoiceRTASRReport
             * 5.融合通信呼叫记录消息：ArtcCdrReport
             * 6.融合通信呼叫中间状态：ArtcTempStatusReport
             */
            String messageType = "SmsReport";//此处应该替换成相应产品的消息类型
            String queueName = CustomConfiguration.getQueueName(); // 在云通信页面开通相应业务消息后，就能在页面上获得对应的queueName,格式类似Alicom-Queue-xxxxxx-SmsReport

            puller.startReceiveMsg(accessKeyId,accessKeySecret, messageType, queueName, messageListener);
        } catch (ClientException e) {
            log.error(e.getMessage(), e);
        }

    }
}
