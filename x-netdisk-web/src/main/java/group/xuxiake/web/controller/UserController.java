package group.xuxiake.web.controller;

import com.aliyuncs.exceptions.ClientException;
import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.entity.param.UserLoginParam;
import group.xuxiake.common.entity.param.UserRegisteParam;
import group.xuxiake.common.enums.LogType;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.web.aspect.SysLogRecord;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.util.SmsSendUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用于用户登录注册的Controller
 * @author xuxiake
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	protected UserService userService;

	/*
	 * 用户登录
	 */
	@RequestMapping(value="/login")
	public Result login(@RequestBody UserLoginParam param) {
		return userService.login(param);
	}

	// @RequestMapping(value="/test")
	// public Result test() throws ClientException {
	// 	SmsSendUtil.SmsSendResult result = SmsSendUtil.regNetDisk("13155834135");
	// 	return new Result(result);
	// }

	@RequestMapping("/getInfo")
	@ResponseBody
	public Result getInfo() {
		return userService.getInfo();
	}

	/**
	 * 用户详情
	 * @return
	 */
	@RequestMapping("/detail")
	@ResponseBody
	public Result detail() {
		return userService.detail();
	}

	/**
	 * 更新用户
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	@ResponseBody
	public Result update(@RequestBody User user) {
		return userService.update(user);
	}

	/**
	 * 更新用户状态
	 * @return
	 */
	@RequestMapping("/updatePrincipal")
	@ResponseBody
	public Result updatePrincipal() {
		userService.updatePrincipal();
		return new Result();
	}

	/**
	 * 上传头像
	 * @param file
	 * @return
	 */
	@RequestMapping("/uploadAvatar")
	@ResponseBody
	public Result uploadAvatar(MultipartFile file) {
		return userService.uploadAvatar(file);
	}

	/**
	 * 注销
	 * @param session
	 * @return
	 */
	@RequestMapping("/logout")
	@SysLogRecord(logType = LogType.LOGOUT, recordContent = false)
	public Result logout(HttpSession session) {
		session.invalidate();
		return new Result();
	}
	
	/*
	 * 用户注册
	 */
	@RequestMapping("/register")
	@ResponseBody
	public Result register(UserRegisteParam param) {
		return userService.register(param);
	}

	/**
	 * 用户APP注册
	 * @param param
	 * @return
	 */
	@RequestMapping("/registerApp")
	@ResponseBody
	public Result registerApp(UserAppRegisteParam param) {
		return userService.registerApp(param);
	}
	
	/*
	 * 检查注册用户名是否可用
	 */
	@RequestMapping("/checkUserName")
	@ResponseBody
	public Result checkUserName(String username) {
		User user = userService.findByName(username);
		Result result = new Result();
		if (user != null) {
			result.setData(NetdiskErrMsgConstant.REGISTER_USERNAME_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.REGISTER_USERNAME_EXIST));
		}
		return result;
	}

	/*
	 * 检查电话是否被注册
	 */
	@RequestMapping("/checkPhone")
	@ResponseBody
	public Result checkPhone(String phone) {
		User user = userService.findByPhone(phone);
		Result result = new Result();
		if (user != null) {
			result.setData(NetdiskErrMsgConstant.REGISTER_PHONE_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.REGISTER_PHONE_EXIST));
		}
		return result;
	}
	
	/*
	 * 检查邮箱是否被注册
	 */
	@RequestMapping("/checkEmail")
	@ResponseBody
	public Result checkEmail(String email) {
		User user = userService.findByEmail(email);
		Result result = new Result();
		if (user != null) {
			result.setData(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST));
		}
		return result;
	}
	
	/*
	 * 检查图片验证码
	 */
	@RequestMapping("/checkImgCode")
	@ResponseBody
	public Result checkImgCode(String imgCode, HttpSession session) {

		return userService.checkImgCode(imgCode, session);
	}

	/**
	 * 创建验证码
	 * @param request
	 * @param response
	 */
	@RequestMapping("/createImg")
	public void createImg(HttpServletRequest request, HttpServletResponse response) {

		userService.createImg(request, response);
	}
	
}
