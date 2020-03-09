package group.xuxiake.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.chat.ChatMessageBase;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.Message;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.mapper.MessageMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
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
    private UserNetdiskMapper userNetdiskMapper;
    @Resource
    private MessageMapper messageMapper;
    @Autowired
    private HttpSession session;
    @Resource
    private AppConfiguration appConfiguration;
    @Resource
    private RedisUtils redisUtils;

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
        UserNetdisk userNetdiskCheck = userNetdiskMapper.selectByPrimaryKey(userId);
        if (userNetdiskCheck != null) {
            if (new Integer(userNetdiskCheck.getUserStatus()) != NetdiskConstant.USER_STATUS_NOT_VERIFY) {
                // 已验证
                result.setData(NetdiskErrMsgConstant.VERIFY_HAVING_BEEN_VERIFIED);
                return result;
            }
        }
        UserNetdisk userNetdisk = new UserNetdisk();
        userNetdisk.setId(userId);
        userNetdisk.setUserStatus(NetdiskConstant.USER_STATUS_NORMAL + "");
        userNetdiskMapper.updateByPrimaryKeySelective(userNetdisk);
        ChatMessageBase chatMessageBase = new ChatMessageBase();
        chatMessageBase.setCreateTime(new Date());
        chatMessageBase.setType("SYSTEM");
        chatMessageBase.setContent("updatePrincipal");
//        ChatWebSocketHandler.sendMessage(userId, JSONObject.toJSONString(chatMessageBase));

        Message message = new Message();
        message.setType(NetdiskConstant.MESSAGE_TYPE_OF_SUCCESS);
        message.setTitle("账户激活成功");
        message.setDescription("您的账户激活成功，所有功能恢复正常使用");
        message.setUserId(userNetdisk.getId());
        messageMapper.insertSelective(message);

        return result;
    }

    /**
     * 给手机发送短信验证码
     * @param userNetdisk
     * @return
     */
    @Override
    public Result sendCodeToPhone(UserNetdisk userNetdisk) {

        Result result = new Result();
        String smsCode = null;
        try {
            smsCode = SmsSendUtil.regNetDisk(userNetdisk.getPhone());
//			smsCode = "1234";
            //业务限流
            if ("isv.BUSINESS_LIMIT_CONTROL".equals(smsCode)) {
                result.setCode(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL);
                result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL));
                return result;
            }
            if (smsCode.length() == 4) {
                redisUtils.set("SMS_CODE:" + session.getId(), smsCode, appConfiguration.getCustomConfiguration().getVerifySmsExpire().longValue());
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