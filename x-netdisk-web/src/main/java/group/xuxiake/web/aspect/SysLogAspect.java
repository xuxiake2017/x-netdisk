package group.xuxiake.web.aspect;

import com.google.gson.Gson;
import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Author by xuxiake, Date on 2022/9/14.
 * 用于记录系统日志的切面
 */
@Component
@Aspect
public class SysLogAspect {

    @Resource
    private SysLogService sysLogService;

    @Pointcut("@annotation(group.xuxiake.web.aspect.SysLogRecord)")
    public void Pointcut() {
    }

    @Around("Pointcut() && @annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLogRecord sysLog) throws Throwable {
        Result result = (Result) point.proceed();
        Object[] args = point.getArgs();
        Gson gson = new Gson();
        String content = (sysLog.recordContent() ? gson.toJson(args[0]) : "").replaceAll("\"password\":\".*\"", "\"password\":\"\"");
        String resultStr = gson.toJson(result).replaceAll("\"token\":\".*\"", "\"token\":\"\"");
        sysLogService.addLog(sysLog.logType(), sysLog.clientType(), content, resultStr);
        return result;
    }
}
