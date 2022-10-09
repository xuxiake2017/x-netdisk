package group.xuxiake.admin.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.SmsLogQueryParams;
import group.xuxiake.common.entity.param.SysLogQueryParams;

public interface LogService {
    Result getSysLog(SysLogQueryParams params);

    Result getSmsLog(SmsLogQueryParams params);
}
