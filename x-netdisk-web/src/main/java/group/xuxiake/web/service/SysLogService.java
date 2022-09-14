package group.xuxiake.web.service;

import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.LogType;

public interface SysLogService {

    public void addLog(LogType logType, ClientType clientType, String content, String result);
}
