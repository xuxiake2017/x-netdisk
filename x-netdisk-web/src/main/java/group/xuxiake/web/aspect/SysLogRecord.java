package group.xuxiake.web.aspect;

import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.LogType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLogRecord {

    LogType logType();
    ClientType clientType() default ClientType.UNSET;
    // 是否记录内容
    boolean recordContent() default true;
}
