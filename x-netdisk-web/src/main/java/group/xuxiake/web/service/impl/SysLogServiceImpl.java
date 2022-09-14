package group.xuxiake.web.service.impl;

import com.google.gson.Gson;
import group.xuxiake.common.entity.SysLog;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.LogType;
import group.xuxiake.common.mapper.SysLogMapper;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.web.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("sysLogService")
public class SysLogServiceImpl implements SysLogService {

    @Resource
    private SysLogMapper sysLogMapper;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private AppConfiguration appConfiguration;
    @Override
    public void addLog(LogType logType, ClientType clientType, String content, String result) {
        SysLog sysLog = new SysLog();
        String host = request.getHeader("host");
        // 获取当前登录对象
        Subject currentUser = SecurityUtils.getSubject();
        User user = (User) currentUser.getPrincipal();
        CustomConfiguration customConfiguration = appConfiguration.getCustomConfiguration();
        Gson gson = new Gson();

        HashMap<String, String> requestHeader = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            if ("cookie".equals(key) || "x-token".equals(key)) {
                continue;
            }
            String value = request.getHeader(key);
            requestHeader.put(key, value);
        }
        String clientIp = requestHeader.get("x-real-ip");
        String httpReferer = requestHeader.get("referer");
        sysLog.setHost(host);
        sysLog.setClientIp(clientIp);

        if (clientType == ClientType.UNSET) {
            if (httpReferer.equals(CustomConfiguration.getServerHost() + "/")) {
                sysLog.setClientType(ClientType.WEB.getValue());
            } else if (httpReferer.equals(CustomConfiguration.getServerHost() + "/app/")) {
                sysLog.setClientType(ClientType.H5.getValue());
            } else if (httpReferer.startsWith("https://servicewechat.com/" + customConfiguration.getWechatAppID())) {
                sysLog.setClientType(ClientType.MINI_APP.getValue());
            } else {
                sysLog.setClientType(ClientType.OTHER.getValue());
            }
        } else {
            sysLog.setClientType(clientType.getValue());
        }
        sysLog.setLogType(logType.getValue());
        if (currentUser.isAuthenticated()) {
            sysLog.setOptUser(user.getUsername());
            sysLog.setOptNickname(user.getNickName());
        }
        sysLog.setContent(content);
        sysLog.setResult(result);
        sysLog.setRequestHeader(gson.toJson(requestHeader));
        sysLogMapper.insertSelective(sysLog);
    }
}
