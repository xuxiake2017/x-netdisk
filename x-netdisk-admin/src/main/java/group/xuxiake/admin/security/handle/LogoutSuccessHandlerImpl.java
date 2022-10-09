package group.xuxiake.admin.security.handle;

import com.alibaba.fastjson.JSON;
import group.xuxiake.admin.security.entity.LoginUser;
import group.xuxiake.admin.util.ServletUtils;
import group.xuxiake.admin.util.StringUtils;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义退出处理类 返回成功
 * 
 * @author ruoyi
 */
@Component("logoutSuccessHandler")
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Resource
    private TokenService tokenService;

    /**
     * 退出处理
     * 
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            String userName = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // TODO 记录用户退出日志
        }
        Result result = new Result();
        result.setMsg("退出成功");
        ServletUtils.renderString(response, JSON.toJSONString(result));
    }
}
