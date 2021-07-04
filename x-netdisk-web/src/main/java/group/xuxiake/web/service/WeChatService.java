package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.WechatSendSMSCaptchaParam;
import group.xuxiake.common.entity.wechat.LoginAndRegisterParam;

import javax.servlet.http.HttpSession;

public interface WeChatService {

    /**
     * 小程序登录、注册
     * @param param
     * @return
     */
    Result loginAndRegister(LoginAndRegisterParam param);

    /**
     * 小程序自动登录
     * @param code
     * @return
     */
    Result autoLogin(String code);

    /**
     * 获取验证码
     * @param uuid
     * @return
     */
    Result getCaptcha(String uuid);

    /**
     * 验证验证码
     * @param uuid
     * @param captcha
     * @return
     */
    Result verifyCaptcha(String uuid, String captcha);

    Result sendSMSCaptcha(WechatSendSMSCaptchaParam param);

    /**
     * 登出（解除绑定）
     * @return
     */
    Result logout(HttpSession session);
}
