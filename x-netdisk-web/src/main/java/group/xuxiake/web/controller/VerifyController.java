package group.xuxiake.web.controller;

import group.xuxiake.common.entity.Result;
import group.xuxiake.web.service.VerifyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	@RequestMapping(value = "/sendCodeToPhone", method = RequestMethod.POST)
	@ResponseBody
	public Result sendCodeToPhone(String phone) {
		return verifyService.sendCodeToPhone(phone);
	}

	/*
	 * 发送注册邮箱验证码，图形验证码用于防止接口被滥用
	 */
	@RequestMapping(value = "/sendCodeToEmail", method = RequestMethod.POST)
	@ResponseBody
	public Result sendCodeToEmail(String email, String imgCode) {
		return verifyService.sendCodeToEmail(email, imgCode);
	}

	@RequestMapping(value = "verifyEmail", method = RequestMethod.GET)
	@ResponseBody
	public Result verifyEmail(String key) {
		return verifyService.verifyEmail(key);
	}

}
