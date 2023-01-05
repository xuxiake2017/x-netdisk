package group.xuxiake.admin.service.impl;

import group.xuxiake.admin.entity.param.LoginParam;
import group.xuxiake.admin.exception.ServiceException;
import group.xuxiake.admin.exception.user.CaptchaException;
import group.xuxiake.admin.exception.user.CaptchaExpireException;
import group.xuxiake.admin.exception.user.UserPasswordNotMatchException;
import group.xuxiake.admin.security.context.AuthenticationContextHolder;
import group.xuxiake.admin.security.entity.LoginUser;
import group.xuxiake.admin.security.handle.TokenService;
import group.xuxiake.admin.service.UserService;
import group.xuxiake.admin.util.CacheConstants;
import group.xuxiake.admin.util.SecurityUtils;
import group.xuxiake.admin.util.StringUtils;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.UserAdmin;
import group.xuxiake.common.util.ImgCodeUtil;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private TokenService tokenService;

    @Override
    public Result login(LoginParam loginParam) {
        String username = loginParam.getUsername();
        String password = loginParam.getPassword();
        String captcha = loginParam.getCaptcha();
        String uuid = loginParam.getUuid();
        Result result = new Result();
        if (StringUtils.isAnyEmpty(username, password, captcha, uuid)) {
            return Result.paramIsNull(result);
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captchaFromRedis = (String) redisUtils.get(verifyKey);
        redisUtils.del(verifyKey);
        if (captcha == null)
        {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!captcha.equalsIgnoreCase(captchaFromRedis))
        {
//            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
//                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
//                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
//        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 生成token
        String token = tokenService.createToken(loginUser);
        Map<String, String> payload = new HashMap<>();
        payload.put("token", token);
        result.setData(payload);
        return result;
    }

    @Override
    public Result getCaptcha() {
        Result result = new Result();
        // 保存验证码信息
        String uuid = UUID.randomUUID().toString();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        Object[] objs = ImgCodeUtil.createImage();
        String imgCode = objs[1].toString();
        BufferedImage bufferedImage = (BufferedImage) objs[0];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpeg", outputStream);
            byte[] bytes = outputStream.toByteArray();
            // 对字节数组Base64编码
            String captchaBASE64 = "data:image/jpeg;base64," + Base64.encodeBase64String(bytes);
            redisUtils.set(verifyKey, imgCode, 5 * 60 * 1000L);
            Map<String, String> payload = new HashMap<>();
            payload.put("captchaBASE64", captchaBASE64);
            payload.put("uuid", uuid);
            payload.put("imgCode", imgCode);
            result.setData(payload);
            return result;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            result.setCode(NetdiskErrMsgConstant.EXCEPTION);
            result.setMsg(e.getMessage());
            return result;
        }
    }

    @Override
    public Result getInfo() {
        UserAdmin user = SecurityUtils.getLoginUser().getUser();
        Result result = new Result();
        result.setData(user);
        return result;
    }
}
