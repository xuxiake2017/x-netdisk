package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.param.WechatSendSMSCaptchaParam;
import group.xuxiake.common.entity.wechat.LoginAndRegisterParam;
import group.xuxiake.web.service.WeChatService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/wechat")
public class WeChatController {

    @Resource
    private WeChatService weChatService;

    /**
     * 小程序登录、注册
     * @param param
     * @return
     */
    @RequestMapping("/loginAndRegister")
    public Result loginAndRegister (@RequestBody LoginAndRegisterParam param) {
        return this.weChatService.loginAndRegister(param);
    }

    /**
     * 小程序自动登录
     * @param code
     * @return
     */
    @RequestMapping("/autoLogin")
    public Result autoLogin (String code) {
        return this.weChatService.autoLogin(code);
    }

    /**
     * 获取验证码
     * @param uuid
     * @return
     */
    @RequestMapping("/getCaptcha")
    public Result getCaptcha (String uuid) {
        return this.weChatService.getCaptcha(uuid);
    }

    /**
     * 验证验证码
     * @param uuid
     * @param captcha
     * @return
     */
    @RequestMapping("/verifyCaptcha")
    public Result verifyCaptcha (String uuid, String captcha) {
        return this.weChatService.verifyCaptcha(uuid, captcha);
    }

    /**
     * 发送短信验证码
     * @return
     */
    @RequestMapping("/sendSMSCaptcha")
    public Result sendSMSCaptcha (@RequestBody WechatSendSMSCaptchaParam param) {
        return this.weChatService.sendSMSCaptcha(param);
    }

    /**
     * 登出（解除绑定）
     * @return
     */
    @RequestMapping("/logout")
    public Result logout(HttpSession session) {
        return this.weChatService.logout(session);
    }
}
