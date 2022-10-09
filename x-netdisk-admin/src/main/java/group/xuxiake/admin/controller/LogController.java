package group.xuxiake.admin.controller;

import group.xuxiake.admin.service.LogService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.SmsLogQueryParams;
import group.xuxiake.common.entity.param.SysLogQueryParams;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/logManage")
public class LogController {

    @Resource
    private LogService logService;

    @PostMapping("/sysLog")
    public Result getSysLog(@RequestBody SysLogQueryParams params) {
        return logService.getSysLog(params);
    }

    @PostMapping("/smsLog")
    public Result getSmsLog(@RequestBody SmsLogQueryParams params) {
        return logService.getSmsLog(params);
    }
}
