package group.xuxiake.admin.security.handle;

import com.alibaba.fastjson.JSON;
import group.xuxiake.admin.util.ServletUtils;
import group.xuxiake.admin.util.StringUtils;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * 认证失败处理类 返回未授权
 * 
 * @author ruoyi
 */
@Component("authenticationEntryPoint")
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable
{
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
    {
        String msg = StringUtils.format("请求访问：{}，认证失败，无法访问系统资源", request.getRequestURI());
        Result result = new Result();
        result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
        result.setMsg(msg);
        ServletUtils.renderString(response, JSON.toJSONString(result));
    }
}
