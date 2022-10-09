package group.xuxiake.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import group.xuxiake.admin.service.LogService;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.SysLog;
import group.xuxiake.common.entity.param.SmsLogQueryParams;
import group.xuxiake.common.entity.param.SysLogQueryParams;
import group.xuxiake.common.mapper.SmsLogMapper;
import group.xuxiake.common.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("logService")
public class LogServiceImpl implements LogService {

    @Resource
    private SysLogMapper sysLogMapper;
    @Resource
    private SmsLogMapper smsLogMapper;

    @Override
    public Result getSysLog(SysLogQueryParams params) {
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        List<SysLog> list = sysLogMapper.getSysLog(params);
        PageInfo<SysLog> pageInfo = new PageInfo<>(list);
        return new Result(pageInfo);
    }

    @Override
    public Result getSmsLog(SmsLogQueryParams params) {
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        List<SmsLog> list = smsLogMapper.getSmsLog(params);
        PageInfo<SmsLog> pageInfo = new PageInfo<>(list);
        return new Result(pageInfo);
    }
}
