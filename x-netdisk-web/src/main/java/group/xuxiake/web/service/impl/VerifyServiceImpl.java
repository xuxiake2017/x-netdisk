package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SmsLog;
import group.xuxiake.common.entity.SysMessage;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.chat.ChatMessageBase;
import group.xuxiake.common.entity.route.RouteOfSendMsgPojo;
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
import group.xuxiake.web.service.VerifyService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service("verifyService")
public class VerifyServiceImpl implements VerifyService {

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
    private SmsLogMapper smsLogMapper;
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

        Result result = new Result();
        String smsCode = null;
        try {
            SmsSendUtil.SmsSendResult smsSendResult = SmsSendUtil.regNetDisk(phone);
            smsCode = smsSendResult.getCode();
//			smsCode = "1234";
            SmsLog smsLog = new SmsLog();
            smsLog.setPhoneNumber(phone);
            smsLog.setSendTime(new Date());
            smsLog.setSuccess(SmsLogSuccess.SUCCESS.getValue());
            smsLog.setMsgContent(smsConfiguration.getTemplateContent(CustomConfiguration.getTemplateCode()));
            smsLog.setBizId(smsSendResult.getResponse().getBizId());
            smsLog.setCode(smsCode);
            smsLog.setErrCode(smsSendResult.getResponse().getCode());
            smsLog.setErrMsg(smsSendResult.getResponse().getMessage());
            //业务限流
            if ("isv.BUSINESS_LIMIT_CONTROL".equals(smsCode)) {
                result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL);
                result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL));
                smsLog.setSuccess(SmsLogSuccess.FAILED.getValue());
                smsLogMapper.insertSelective(smsLog);
                return result;
            }
            if (smsCode.length() == 4) {
                redisUtils.set("SMS_CODE:" + session.getId(), smsCode, appConfiguration.getCustomConfiguration().getVerifySmsExpire().longValue());
                smsLogMapper.insertSelective(smsLog);
                return result;
            }
        } catch (Exception e) {
            log.error("短信验证码发送失败", e);
        }
        result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED);
        result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED));
        return result;
    }
}
