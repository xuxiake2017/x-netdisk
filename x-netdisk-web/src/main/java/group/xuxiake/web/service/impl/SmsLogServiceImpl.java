package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.mapper.SmsLogMapper;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.web.service.SmsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service("smsLogService")
public class SmsLogServiceImpl implements SmsLogService {

    @Resource
    private SmsLogMapper smsLogMapper;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private CustomConfiguration customConfiguration;

    @Override
    public void addLog(SmsLog smsLog) {
        String clientIp = request.getHeader("x-real-ip");
        String httpReferer = request.getHeader("referer");
        if (smsLog.getClientType() == null || smsLog.getClientType() == ClientType.UNSET.getValue()) {
            if (httpReferer.equals(CustomConfiguration.getServerHost() + "/")) {
                smsLog.setClientType(ClientType.WEB.getValue());
            } else if (httpReferer.equals(CustomConfiguration.getServerHost() + "/app/")) {
                smsLog.setClientType(ClientType.H5.getValue());
            } else if (httpReferer.startsWith("https://servicewechat.com/" + customConfiguration.getWechatAppID())) {
                smsLog.setClientType(ClientType.MINI_APP.getValue());
            } else {
                smsLog.setClientType(ClientType.OTHER.getValue());
            }
        }
        smsLog.setClientIp(clientIp);
        smsLogMapper.insertSelective(smsLog);
    }
}
