package group.xuxiake.web.service.impl;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.google.gson.Gson;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.WechatUser;
import group.xuxiake.common.entity.param.WechatSendSMSCaptchaParam;
import group.xuxiake.common.entity.wechat.AuthCode2SessionRes;
import group.xuxiake.common.entity.wechat.LoginAndRegisterParam;
import group.xuxiake.common.entity.wechat.WechatUserInfo;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.LogType;
import group.xuxiake.common.enums.SmsLogSuccess;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.mapper.WechatUserMapper;
import group.xuxiake.common.util.*;
import group.xuxiake.web.aspect.SysLogRecord;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.web.configuration.SmsConfiguration;
import group.xuxiake.web.service.SmsLogService;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.service.WeChatService;
import group.xuxiake.web.shiro.AutoLoginToken;
import group.xuxiake.web.shiro.LoginType;
import group.xuxiake.web.util.FastDFSClientWrapper;
import group.xuxiake.web.util.PasswordEncoder;
import group.xuxiake.web.util.SmsSendUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.*;

@Slf4j
@Service("weChatService")
public class WeChatServiceImpl implements WeChatService {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private CustomConfiguration customConfiguration;
    @Resource
    private FastDFSClientWrapper fastDFSClientWrapper;
    @Resource
    private WechatUserMapper wechatUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private UserService userService;
    @Autowired
    private HttpSession httpSession;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SmsLogService smsLogService;
    @Resource
    private SmsConfiguration smsConfiguration;
    /**
     * 小程序登录、注册
     * @param param
     * @return
     */
    @SysLogRecord(logType = LogType.REGISTER, clientType = ClientType.MINI_APP)
    @Override
    @Transactional
    public Result loginAndRegister(LoginAndRegisterParam param) {

        Result result = new Result();

        WechatUserInfo wechatUserInfo = param.getWechatUserInfo();
        String code = param.getCode();
        String phone = param.getPhone();
        String uuid = param.getUuid();
        String smsCaptcha = param.getSmsCaptcha();
        if (wechatUserInfo == null || StringUtils.isAnyEmpty(code, phone, uuid, smsCaptcha)) {
            result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
            return result;
        }
        String smsCaptchaFromRedis = (String) redisUtils.get(CustomConfiguration.getTemplateCode() + uuid);
        if (StringUtils.isAnyEmpty(smsCaptchaFromRedis) || !smsCaptchaFromRedis.equals(smsCaptcha)) {
            result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
            result.setMsg("短信验证码超时或错误");
            return result;
        }

        AuthCode2SessionRes authCode2SessionRes = this.jscode2session(code);
        if (authCode2SessionRes.getErrcode() != 0) {
            result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
            result.setMsg(authCode2SessionRes.getErrmsg());
            return result;
        }

        String openid = authCode2SessionRes.getOpenid();
        User userByPhone = userService.findByPhone(phone);
        WechatUser wechatUserByOpenid = wechatUserMapper.findByOpenid(openid);
        if (wechatUserByOpenid != null && userByPhone != null) { // 已注册，自动登录（该手机号在系统中有账号，该用户小程序登录过）

            if (wechatUserMapper.findByUserId(userByPhone.getId()) != null) {
                result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                result.setMsg("该手机号已注册！");
                return result;
            }

            wechatUserByOpenid.setUserId(userByPhone.getId());
            wechatUserMapper.updateByPrimaryKey(wechatUserByOpenid);
            // 获取当前登录对象
            Subject currentUser = SecurityUtils.getSubject();
            // 判断是否登陆
            if(!currentUser.isAuthenticated()) {
                AutoLoginToken autoLoginToken = new AutoLoginToken(openid, "", LoginType.NO_PASSWORD);
                try {
                    // 进行登陆
                    currentUser.login(autoLoginToken);
                } catch (UnknownAccountException uae) { // 未知用户名
                    result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                    result.setMsg(uae.getMessage());
                    return result;
                }
            }
            redisUtils.del(CustomConfiguration.getTemplateCode() + uuid);
            result.setData(httpSession.getId());
            result.setMsg("登录成功！");
            return result;
        } else if (wechatUserByOpenid == null && userByPhone != null){ // 该手机号在系统中有账号，该用户在小程序未登录过
            WechatUser wechatUser = new WechatUser(wechatUserInfo);
            wechatUser.setOpenid(authCode2SessionRes.getOpenid());
            wechatUser.setUserId(userByPhone.getId());
            wechatUserMapper.insertSelective(wechatUser);

            // 获取当前登录对象
            Subject currentUser = SecurityUtils.getSubject();
            // 判断是否登陆
            if(!currentUser.isAuthenticated()) {
                AutoLoginToken autoLoginToken = new AutoLoginToken(openid, "", LoginType.NO_PASSWORD);
                try {
                    //进行登陆
                    currentUser.login(autoLoginToken);
                } catch (UnknownAccountException uae) { // 未知用户名
                    result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                    result.setMsg(uae.getMessage());
                    return result;
                }
            }
            redisUtils.del(CustomConfiguration.getTemplateCode() + uuid);
            result.setData(httpSession.getId());
            result.setMsg("登录成功！");
            return result;
        } else if (wechatUserByOpenid != null && userByPhone == null){ // 该手机号在系统中没有账号，但该用户在小程序登录过
            // 获取微信用户头像并上传到服务器
            ResponseEntity<byte[]> entity = restTemplate.getForEntity(wechatUserInfo.getAvatarUrl(), byte[].class);
            byte[] bytes = entity.getBody();
            assert bytes != null;
            String path = "";
            try(InputStream is = new ByteArrayInputStream(bytes)) {
                path = fastDFSClientWrapper.uploadFile(is, bytes.length, "jpg");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                result.setCode(NetdiskErrMsgConstant.EXCEPTION);
                result.setMsg("头像上传失败");
                return result;
            }

            User user = new User();
            user.setUsername(phone);
            user.setPassword(PasswordEncoder.encode(phone.substring(5, 11), phone));
            user.setSex(wechatUserInfo.getGender());
            user.setRealName("");
            user.setRegTime(new Date());
            user.setTotalMemory(appConfiguration.getCustomConfiguration().getTotalMemory());
            user.setUsedMemory(0L);
            user.setPhone(phone);
            user.setEmail("");
            user.setUserStatus(NetdiskConstant.USER_STATUS_NORMAL);
            user.setAvatar(appConfiguration.getFdfsNginxServer() + "/" + path);
            user.setNickName(wechatUserInfo.getNickName());
            userMapper.insertSelective(user);

            wechatUserByOpenid.setUserId(user.getId());
            wechatUserMapper.updateByPrimaryKey(wechatUserByOpenid);

            // 获取当前登录对象
            Subject currentUser = SecurityUtils.getSubject();
            // 判断是否登陆
            if(!currentUser.isAuthenticated()) {
                AutoLoginToken autoLoginToken = new AutoLoginToken(openid, "", LoginType.NO_PASSWORD);
                try {
                    //进行登陆
                    currentUser.login(autoLoginToken);
                } catch (UnknownAccountException uae) { // 未知用户名
                    result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                    result.setMsg(uae.getMessage());
                    return result;
                }
            }
            redisUtils.del(CustomConfiguration.getTemplateCode() + uuid);
            result.setData(httpSession.getId());
            result.setMsg("登录成功！");
            return result;
        } else { // 该手机号在系统中没有账号，且该用户未在小程序登录过
            // 获取微信用户头像并上传到服务器
            ResponseEntity<byte[]> entity = restTemplate.getForEntity(wechatUserInfo.getAvatarUrl(), byte[].class);
            byte[] bytes = entity.getBody();
            assert bytes != null;
            String path = "";
            try(InputStream is = new ByteArrayInputStream(bytes)) {
                path = fastDFSClientWrapper.uploadFile(is, bytes.length, "jpg");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                result.setCode(NetdiskErrMsgConstant.EXCEPTION);
                result.setMsg("头像上传失败");
                return result;
            }

            User user = new User();
            user.setUsername(phone);
            user.setPassword(PasswordEncoder.encode(phone.substring(5, 11), phone));
            user.setSex(wechatUserInfo.getGender());
            user.setRealName("");
            user.setRegTime(new Date());
            user.setTotalMemory(appConfiguration.getCustomConfiguration().getTotalMemory());
            user.setUsedMemory(0L);
            user.setPhone(phone);
            user.setEmail("");
            user.setUserStatus(NetdiskConstant.USER_STATUS_NORMAL);
            user.setAvatar(appConfiguration.getFdfsNginxServer() + "/" + path);
            user.setNickName(wechatUserInfo.getNickName());
            userMapper.insertSelective(user);

            WechatUser wechatUser = new WechatUser(wechatUserInfo);
            wechatUser.setOpenid(authCode2SessionRes.getOpenid());
            wechatUser.setUserId(user.getId());
            wechatUserMapper.insertSelective(wechatUser);

            // 获取当前登录对象
            Subject currentUser = SecurityUtils.getSubject();
            // 判断是否登陆
            if(!currentUser.isAuthenticated()) {
                AutoLoginToken autoLoginToken = new AutoLoginToken(openid, "", LoginType.NO_PASSWORD);
                try {
                    //进行登陆
                    currentUser.login(autoLoginToken);
                } catch (UnknownAccountException uae) { // 未知用户名
                    result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                    result.setMsg(uae.getMessage());
                    return result;
                }
            }
            redisUtils.del(CustomConfiguration.getTemplateCode() + uuid);
            result.setData(httpSession.getId());
            result.setMsg("登录成功！");
            return result;
        }

    }

    /**
     * 小程序自动登录
     * @param code
     * @return
     */
    @SysLogRecord(logType = LogType.LOGIN, clientType = ClientType.MINI_APP)
    @Override
    public Result autoLogin(String code) {
        Result result = new Result();
        AuthCode2SessionRes authCode2SessionRes = this.jscode2session(code);
        if (authCode2SessionRes.getErrcode() != 0) {
            result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
            result.setMsg(authCode2SessionRes.getErrmsg());
            return result;
        }
        String openid = authCode2SessionRes.getOpenid();
        // 获取当前登录对象
        Subject currentUser = SecurityUtils.getSubject();
        // 判断是否登陆
        if(!currentUser.isAuthenticated()) {
            AutoLoginToken autoLoginToken = new AutoLoginToken(openid, "", LoginType.NO_PASSWORD);
            try {
                //进行登陆
                currentUser.login(autoLoginToken);
            } catch (UnknownAccountException uae) { // 未知用户名
                result.setCode(NetdiskErrMsgConstant.LOGIN_ACCOUNT_ERR);
                result.setMsg(uae.getMessage());
                return result;
            }
        }
        result.setData(httpSession.getId());
        return result;
    }

    /**
     * 获取验证码
     * @param uuidDel
     * @return
     */
    @Override
    public Result getCaptcha(String uuidDel) {

        Result result = new Result();

        if (!StringUtils.isAnyEmpty(uuidDel)) {
            redisUtils.del(appConfiguration.getCaptchaPrefix() + uuidDel);
        }

        Object[] objs = ImgCodeUtil.createImage();
        String imgCode = objs[1].toString();
        BufferedImage bufferedImage = (BufferedImage) objs[0];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpeg", outputStream);
            byte[] bytes = outputStream.toByteArray();
            // 对字节数组Base64编码
            String captchaBASE64 = "data:image/jpeg;base64," + Base64.encodeBase64String(bytes);
            String uuid = UUID.randomUUID().toString();
            redisUtils.set(appConfiguration.getCaptchaPrefix() + uuid, imgCode, appConfiguration.getCaptchaTimeout());
            Map<String, String> payload = new HashMap<>();
            payload.put("captchaBASE64", captchaBASE64);
            payload.put("uuid", uuid);
            result.setData(payload);
            return result;
        } catch (IOException e) {
           log.error(e.getMessage(), e);
            result.setCode(NetdiskErrMsgConstant.EXCEPTION);
            return result;
        }
    }

    /**
     * 验证验证码
     * @param uuid
     * @param captcha
     * @return
     */
    @Override
    public Result verifyCaptcha(String uuid, String captcha) {

        Result result = new Result();
        result.setMsg("验证通过");
        if (StringUtils.isAnyEmpty(uuid, captcha)) {
            result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
            return result;
        }
        String captchaFromRedis = (String) redisUtils.get(appConfiguration.getCaptchaPrefix() + uuid);
        if (StringUtils.isAnyEmpty(captchaFromRedis)) {
            result.setCode(NetdiskErrMsgConstant.LOGIN_CAPTCHA_TIMEOUT);
            return result;
        }
        if (!captchaFromRedis.equalsIgnoreCase(captcha)) {
            result.setCode(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR);
            return result;
        }
        redisUtils.del(appConfiguration.getCaptchaPrefix() + uuid);
        return result;
    }

    @Override
    public Result sendSMSCaptcha(WechatSendSMSCaptchaParam param) {

        Result result = new Result();
        try {
            if (StringUtils.isAnyEmpty(param.get_sign(), param.get_appid(), param.get_timestamp(), param.getPhone())) {
                result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
                return result;
            }
            String phone = param.getPhone();
            long current = new Date().getTime();
            long timestamp = Long.parseLong(param.get_timestamp());
            if (timestamp - current >= 20000 || current - timestamp >= 20000) {
                result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                result.setMsg("时间戳校验失败");
                return result;
            }
            PrivateKey privateKey = RSAUtil.getPrivateKey("private.key");
            String decryptedText = RSAUtil.decryptText(param.get_sign(), privateKey);
            String sign = RSAUtil.makeSign(param, customConfiguration.getWechatAppID());
            if (!decryptedText.equals(sign)) {
                result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
                result.setMsg("签名不一致");
                return result;
            }
            SmsSendUtil.SmsSendResult smsSendResult = SmsSendUtil.regNetDisk(phone);
            SendSmsVerifyCodeResponse response = smsSendResult.getResponse();
            String smsCode = smsSendResult.getCode();
//			smsCode = "1234";
            SmsLog smsLog = new SmsLog();
            smsLog.setPhoneNumber(phone);
            smsLog.setSendTime(new Date());
            smsLog.setCode(smsCode);
            smsLog.setMsgContent(smsConfiguration.getTemplateContent(CustomConfiguration.getTemplateCode()));
            if (response != null) {
                SendSmsVerifyCodeResponseBody body = response.getBody();
                SendSmsVerifyCodeResponseBody.Model model = body.getModel();
                smsLog.setBizId(model.getBizId());
                smsLog.setErrCode(body.getCode());
                smsLog.setErrMsg(body.getMessage());
                if (body.getSuccess()) {
                    smsLog.setSuccess(SmsLogSuccess.SUCCESS.getValue());
                } else {
                    smsLog.setSuccess(SmsLogSuccess.FAILED.getValue());
                }
            } else {
                smsLog.setSuccess(SmsLogSuccess.FAILED.getValue());
            }
            smsLog.setClientType(ClientType.UNSET.getValue());
            String uuid = UUID.randomUUID().toString();
            result.setData(uuid);
            redisUtils.set(CustomConfiguration.getTemplateCode() + uuid, smsCode, appConfiguration.getCustomConfiguration().getVerifySmsExpire().longValue());
            smsLogService.addLog(smsLog);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED);
            result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED));
            return result;
        }
        result.setMsg("验证码发送成功");
        return result;
    }

    /**
     * 登出（解除绑定）
     * @return
     */
    @SysLogRecord(logType = LogType.LOGOUT, clientType = ClientType.MINI_APP, recordContent = false)
    @Override
    public Result logout(HttpSession session) {

        User user = (User) SecurityUtils.getSubject().getPrincipal();
        WechatUser wechatUser = wechatUserMapper.findByUserId(user.getId());
        wechatUser.setUserId(null);
        wechatUserMapper.updateByPrimaryKey(wechatUser);
        session.invalidate();
        return new Result();
    }

    private AuthCode2SessionRes jscode2session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={SECRET}&js_code={JSCODE}&{grant_type}=authorization_code";
        Map<String, String> params = new HashMap<>();
        params.put("APPID", customConfiguration.getWechatAppID());
        params.put("SECRET", customConfiguration.getWechatAppSecret());
        params.put("JSCODE", code);
        params.put("grant_type", "authorization_code");
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class, params);
        AuthCode2SessionRes authCode2SessionRes = new Gson().fromJson(entity.getBody(), AuthCode2SessionRes.class);
        return authCode2SessionRes;
    }
}
