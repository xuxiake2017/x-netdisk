package group.xuxiake.web.exception;

import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionResolver {

    /**
     * 全局异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e){

        log.error(e.getMessage(),e);
        Result result = new Result();
        result.setCode(NetdiskErrMsgConstant.EXCEPTION);
        result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.EXCEPTION));
        return result;
    }
}
