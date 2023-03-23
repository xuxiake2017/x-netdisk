package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.VerifyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * 注册验证信息（电话邮箱）controller
 * @author xuxiake
 *
 */
@Controller
@RequestMapping("/verify")
public class VerifyController {

	@Resource
	private VerifyService verifyService;

	/*
	 * 发送短信验证码
	 */
	@RequestMapping(value = "/sendCodeToPhone")
	@ResponseBody
	public Result sendCodeToPhone(String phone) {
//		return verifyService.sendCodeToPhone(phone);
		return null;
	}

	@RequestMapping("verifyEmail")
	@ResponseBody
	public Result verifyEmail(String key) {
		return verifyService.verifyEmail(key);
	}

}
