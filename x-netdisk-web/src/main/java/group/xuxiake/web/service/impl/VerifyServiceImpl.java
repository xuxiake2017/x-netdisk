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

        Result result = new Result();
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
            redisUtils.set("SMS_CODE:" + session.getId(), smsCode, appConfiguration.getCustomConfiguration().getVerifySmsExpire().longValue());
            smsLogService.addLog(smsLog);
            return result;
        } catch (Exception e) {
            log.error("短信验证码发送失败", e);
        }
        result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED);
        result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED));
        return result;
    }
}
