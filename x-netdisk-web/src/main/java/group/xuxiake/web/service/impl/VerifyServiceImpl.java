package group.xuxiake.web.service.impl;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.SysMessage;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.chat.ChatMessageBase;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.SmsLogSuccess;
import group.xuxiake.common.mapper.SmsLogMapper;
import group.xuxiake.common.mapper.SysMessageMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.web.configuration.SmsConfiguration;
import group.xuxiake.web.service.RouteService;
import group.xuxiake.web.service.SmsLogService;
import group.xuxiake.web.service.VerifyService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service("verifyService")
public class VerifyServiceImpl implements VerifyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Resource
    private UserMapper userMapper;
    @Resource
    private SysMessageMapper messageMapper;
    @Autowired
    private HttpSession session;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private RouteService routeService;
    @Resource
    private SmsLogService smsLogService;
    @Resource
    private SmsConfiguration smsConfiguration;

    /**
     * 验证邮箱
     * @param key
     * @return
     */
    @Override
    public Result verifyEmail(String key) {
        Result result = new Result();
        if (key == null || "".equals(key)) {
            result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
            result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.PARAM_IS_NULL));
            return result;
        }
        Map<String, Object> map = (Map<String, Object>) redisUtils.get("EMAIL_KEY:" + key);
        if (map == null || map.size() == 0) {
            result.setCode(NetdiskErrMsgConstant.VERIFY_TIME_OUT);
            result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.VERIFY_TIME_OUT));
            return result;
        }
        Integer userId = (Integer) map.get("id");
        User userCheck = userMapper.selectByPrimaryKey(userId);
        if (userCheck != null) {
            if (new Integer(userCheck.getUserStatus()) != NetdiskConstant.USER_STATUS_NOT_VERIFY) {
                // 已验证
                result.setData(NetdiskErrMsgConstant.VERIFY_HAVING_BEEN_VERIFIED);
                return result;
            }
        }
        User user = new User();
        user.setId(userId);
        user.setUserStatus(NetdiskConstant.USER_STATUS_NORMAL);
        userMapper.updateByPrimaryKeySelective(user);
        ChatMessageBase chatMessageBase = new ChatMessageBase();
        chatMessageBase.setCreateTime(new Date());
        chatMessageBase.setType("SYSTEM");
        chatMessageBase.setContent("updatePrincipal");
        RouteOfSendMsgPojo data = new RouteOfSendMsgPojo();
        data.setUserId(userId);
        data.setChatMessageBase(chatMessageBase);
        routeService.postMsgToRoute(userId.toString(), appConfiguration.getSendMsgToUserPath(), data, Result.class);

        SysMessage message = new SysMessage();
        message.setType(NetdiskConstant.MESSAGE_TYPE_OF_SUCCESS);
        message.setTitle("账户激活成功");
        message.setDescription("您的账户激活成功，所有功能恢复正常使用");
        message.setUserId(user.getId());
        messageMapper.insertSelective(message);

        return result;
    }

    /**
     * 给手机发送短信验证码
     * @param phone
     * @return
     */
    @Override
    public Result sendCodeToPhone(String phone) {
		return sendPhoneCode(phone, "SMS_RATE:" + phone, "SMS_CODE:" + session.getId());
	}

	private Result sendPhoneCode(String phone, String rateLimitKey, String codeKey) {
		Result result = new Result();
		if (StringUtils.isBlank(phone) || !phone.matches("^1\\d{10}$")) {
			return Result.paramIsNull(result);
		}
		CustomConfiguration custom = appConfiguration.getCustomConfiguration();
		if (!redisUtils.setIfAbsent(rateLimitKey, Boolean.TRUE, custom.getSmsSendInterval())) {
			result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL);
			return result;
		}
		String smsCode = null;
        try {
            SmsSendUtil.SmsSendResult smsSendResult = SmsSendUtil.regNetDisk(phone);
            SendSmsVerifyCodeResponse response = smsSendResult.getResponse();
            smsCode = smsSendResult.getCode();
//			smsCode = "1234";
            SmsLog smsLog = new SmsLog();
            smsLog.setPhoneNumber(phone);
            smsLog.setSendTime(new Date());
            smsLog.setCode(smsCode);
            smsLog.setMsgContent(smsConfiguration.getTemplateContent(CustomConfiguration.getTemplateCode()));
            boolean sendSuccess = false;
            if (response != null) {
                SendSmsVerifyCodeResponseBody body = response.getBody();
                SendSmsVerifyCodeResponseBody.Model model = body == null ? null : body.getModel();
                if (model != null) {
                    smsLog.setBizId(model.getBizId());
                }
                if (body != null) {
                    smsLog.setErrCode(body.getCode());
                    smsLog.setErrMsg(body.getMessage());
                }
                if (body != null && Boolean.TRUE.equals(body.getSuccess())) {
                    smsLog.setSuccess(SmsLogSuccess.SUCCESS.getValue());
                    sendSuccess = true;
                } else {
                    smsLog.setSuccess(SmsLogSuccess.FAILED.getValue());
                }
            } else {
                smsLog.setSuccess(SmsLogSuccess.FAILED.getValue());
            }
            smsLog.setClientType(ClientType.UNSET.getValue());
            smsLogService.addLog(smsLog);
            if (!sendSuccess || StringUtils.isBlank(smsCode)) {
                throw new IllegalStateException("短信服务未返回成功状态");
            }
            if (!redisUtils.set(codeKey, smsCode, custom.getVerifySmsExpire().longValue())) {
                throw new IllegalStateException("短信验证码写入 Redis 失败");
            }
            return result;
        } catch (Exception e) {
            redisUtils.del(codeKey, rateLimitKey);
            log.error("短信验证码发送失败", e);
        }
        result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED);
        result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED));
        return result;
    }

	@Override
	public Result sendPhoneChangeCode(Integer userId, String phone) {
		Result result = new Result();
		if (userId == null || StringUtils.isBlank(phone) || !phone.matches("^1\\d{10}$")) {
			return Result.paramIsNull(result);
		}
		User current = userMapper.selectByPrimaryKey(userId);
		User existing = userMapper.findAnyByPhone(phone);
		if (current == null || (existing != null && !userId.equals(existing.getId()))) {
			result.setCode(NetdiskErrMsgConstant.USER_PHONE_EXIST);
			return result;
		}
		if (phone.equals(current.getPhone())) {
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("新手机号不能与当前手机号相同");
			return result;
		}
		redisUtils.del("PHONE_CHANGE_ATTEMPTS:" + userId + ":" + phone);
		return sendPhoneCode(phone, "PHONE_CHANGE_RATE:" + userId + ":" + phone,
				"PHONE_CHANGE_CODE:" + userId + ":" + phone);
	}

    @Override
    public Result sendCodeToEmail(String email, String imgCode) {
        Result result = new Result();
        String normalizedEmail = EmailAddressUtil.normalize(email);
        if (!EmailAddressUtil.isValid(normalizedEmail) || StringUtils.isBlank(imgCode)) {
            return Result.paramIsNull(result);
        }

        String captchaKey = appConfiguration.getCaptchaPrefix() + session.getId();
        String expectedImgCode = (String) redisUtils.get(captchaKey);
        if (StringUtils.isBlank(expectedImgCode)) {
            result.setCode(NetdiskErrMsgConstant.REGISTER_CAPTCHA_TIMEOUT);
            return result;
        }
        if (!expectedImgCode.equalsIgnoreCase(imgCode.trim())) {
            result.setCode(NetdiskErrMsgConstant.REGISTER_IMG_CODE_ERR);
            return result;
        }
        if (!redisUtils.compareAndDelete(captchaKey, expectedImgCode)) {
            result.setCode(NetdiskErrMsgConstant.REGISTER_CAPTCHA_TIMEOUT);
            return result;
        }

        User query = new User();
        query.setEmail(normalizedEmail);
        if (userMapper.findByLoginInfo(query) != null) {
            result.setCode(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST);
            return result;
        }

        CustomConfiguration custom = appConfiguration.getCustomConfiguration();
        String rateLimitKey = "EMAIL_REGISTER_RATE:" + normalizedEmail;
        if (!redisUtils.setIfAbsent(rateLimitKey, Boolean.TRUE, custom.getEmailSendInterval())) {
            result.setCode(NetdiskErrMsgConstant.SEND_EMAIL_CODE_BUSINESS_LIMIT_CONTROL);
            return result;
        }

        String codeKey = "EMAIL_REGISTER_CODE:" + normalizedEmail;
        String attemptsKey = "EMAIL_REGISTER_ATTEMPTS:" + normalizedEmail;
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        try {
            redisUtils.del(attemptsKey);
            if (!redisUtils.set(codeKey, code, custom.getEmailCodeExpire())) {
                throw new IllegalStateException("邮箱验证码写入 Redis 失败");
            }
            long expireMinutes = Math.max(1L, custom.getEmailCodeExpire() / 60000L);
            EmailSendUtil.sendVerificationCode(normalizedEmail, code, expireMinutes);
            return result;
        } catch (Exception e) {
            redisUtils.del(codeKey, attemptsKey, rateLimitKey);
            log.error("注册邮箱验证码发送失败: {}", normalizedEmail, e);
            result.setCode(NetdiskErrMsgConstant.SEND_EMAIL_CODE_FAILED);
            return result;
        }
    }

	@Override
	public Result sendEmailChangeCode(Integer userId, String email) {
		Result result = new Result();
		String normalizedEmail = EmailAddressUtil.normalize(email);
		if (userId == null || !EmailAddressUtil.isValid(normalizedEmail)) {
			return Result.paramIsNull(result);
		}
		User current = userMapper.selectByPrimaryKey(userId);
		User existing = userMapper.findAnyByEmail(normalizedEmail);
		if (current == null || (existing != null && !userId.equals(existing.getId()))) {
			result.setCode(NetdiskErrMsgConstant.USER_EMAIL_EXIST);
			return result;
		}
		if (normalizedEmail.equals(current.getEmail())) {
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("新邮箱不能与当前邮箱相同");
			return result;
		}

		CustomConfiguration custom = appConfiguration.getCustomConfiguration();
		String rateKey = "EMAIL_CHANGE_RATE:" + userId + ":" + normalizedEmail;
		if (!redisUtils.setIfAbsent(rateKey, Boolean.TRUE, custom.getEmailSendInterval())) {
			result.setCode(NetdiskErrMsgConstant.SEND_EMAIL_CODE_BUSINESS_LIMIT_CONTROL);
			return result;
		}
		String codeKey = "EMAIL_CHANGE_CODE:" + userId + ":" + normalizedEmail;
		String attemptsKey = "EMAIL_CHANGE_ATTEMPTS:" + userId + ":" + normalizedEmail;
		String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
		try {
			redisUtils.del(attemptsKey);
			if (!redisUtils.set(codeKey, code, custom.getEmailCodeExpire())) {
				throw new IllegalStateException("邮箱验证码写入 Redis 失败");
			}
			EmailSendUtil.sendEmailChangeCode(normalizedEmail, code,
					Math.max(1L, custom.getEmailCodeExpire() / 60000L));
			return result;
		} catch (Exception e) {
			redisUtils.del(codeKey, attemptsKey, rateKey);
			log.error("修改邮箱验证码发送失败: {}", normalizedEmail, e);
			result.setCode(NetdiskErrMsgConstant.SEND_EMAIL_CODE_FAILED);
			return result;
		}
	}
}
