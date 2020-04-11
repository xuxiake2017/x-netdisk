package group.xuxiake.web.service;

import group.xuxiake.common.entity.Result;

public interface VerifyService {
    /**
     * 验证邮箱
     * @param key
     * @return
     */
    Result verifyEmail(String key);

    /**
     * 给手机发送短信验证码
     * @param phone
     * @return
     */
    Result sendCodeToPhone(String phone);
}
