package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.util.NetdiskConstant;
import group.xuxiake.common.util.NetdiskErrMsgConstant;
import group.xuxiake.common.util.RedisUtils;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.common.entity.Message;
import group.xuxiake.common.entity.UserNetdisk;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.entity.show.UserFriendListShow;
import group.xuxiake.common.entity.show.UserNetdiskShowInfo;
import group.xuxiake.web.exception.UserRepeatLoginException;
import group.xuxiake.common.mapper.MessageMapper;
import group.xuxiake.common.mapper.UserFriendListMapper;
import group.xuxiake.common.mapper.UserNetdiskMapper;
import group.xuxiake.web.service.UserNetdiskService;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service("userNetdiskService")
public class UserNetdiskServiceImpl implements UserNetdiskService {

	@Resource
	private UserNetdiskMapper userNetdiskMapper;
	@Autowired
	private HttpSession session;
	@Resource
	private MessageMapper messageMapper;
	@Resource
	private UserFriendListMapper userFriendListMapper;
	@Resource
	private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private RedisUtils redisUtils;

	@Override
	public UserNetdisk findByName(String userName) {
		UserNetdisk userNetdisk = new UserNetdisk();
		userNetdisk.setUsername(userName);
		return userNetdiskMapper.findByLoginInfo(userNetdisk);
	}

	@Override
	public UserNetdisk findByPhone(String phone) {
		UserNetdisk userNetdisk = new UserNetdisk();
		userNetdisk.setPhone(phone);
		return userNetdiskMapper.findByLoginInfo(userNetdisk);
	}

	@Override
	public UserNetdisk findByEmail(String email) {
		UserNetdisk userNetdisk = new UserNetdisk();
		userNetdisk.setEmail(email);
		return userNetdiskMapper.findByLoginInfo(userNetdisk);
	}

	@Transactional
	@Override
	public Result register(UserNetdisk userNetdisk) {

		Result result = new Result();
		String smsCodeFromRedis = (String) redisUtils.get("SMS_CODE:" + session.getId());
		if(userNetdisk.getVerifyInfo().contains("@")){
			userNetdisk.setEmail(userNetdisk.getVerifyInfo());
		} else {
			userNetdisk.setPhone(userNetdisk.getVerifyInfo());
			if (smsCodeFromRedis == null || "".equals(smsCodeFromRedis)) {
				result.setCode(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_TIME_OUT);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_TIME_OUT));
				return result;
			}
			if (!smsCodeFromRedis.equals(userNetdisk.getSmsCode())) {
				result.setCode(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_ERR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_ERR));
				return result;
			}
		}
		Date regTime = new Date();
		userNetdisk.setRegTime(regTime);
		userNetdisk.setTotalMemory(appConfiguration.getCustomConfiguration().getTotalMemory());
		userNetdisk.setUsedMemory("0");
		userNetdisk.setUserStatus(NetdiskConstant.USER_STATUS_NOT_VERIFY + "");

		String key = UUID.randomUUID().toString();
		EmailSendUtil.regNetDisk(userNetdisk.getEmail(), userNetdisk.getUsername(), key);

		userNetdisk.setPassword(PasswordEncoder.encode(userNetdisk.getPassword(), userNetdisk.getUsername()));
		userNetdisk.setAvatar(appConfiguration.getCustomConfiguration().getServerHost() + "/logo/default_avatar.gif");
		userNetdiskMapper.insertSelective(userNetdisk);

		// 添加消息
		Message message = new Message();
		message.setType(NetdiskConstant.MESSAGE_TYPE_OF_WARNING);
		message.setTitle("账户激活");
		message.setDescription("您的账户尚未激活，激活链接已经发送到您邮箱，请尽快激活");
		message.setUserId(userNetdisk.getId());
		messageMapper.insertSelective(message);

		message.setType(NetdiskConstant.MESSAGE_TYPE_OF_SUCCESS);
		message.setTitle("注册成功");
		message.setDescription("恭喜您注册成功，网盘初始容量为" + new Double(appConfiguration.getCustomConfiguration().getTotalMemory()) / (1024*1024) + "M");
		messageMapper.insertSelective(message);

		if(userNetdisk.getVerifyInfo().contains("@")){
			Map<String, Object> map = new HashMap<>();
			map.put("key", key);
			map.put("id", userNetdisk.getId());
			redisUtils.set("EMAIL_KEY:" + key, map, appConfiguration.getCustomConfiguration().getVerifyEmailExpire().longValue());
		}
		return result;
	}

	@Override
	public UserNetdisk findById(Integer id) {
		return userNetdiskMapper.selectByPrimaryKey(id);
	}

	@Override
	public Integer updateUser(UserNetdisk user) {
		return userNetdiskMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public Result detail() {
		Result result = new Result();
		UserNetdisk user = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
		user.setPassword(null);
		result.setData(user);
		return result;
	}

	@Transactional
	@Override
	public Result update(UserNetdisk user) {
		Result result = new Result();
		if (user.getPassword() != null && !"".equals(user.getPassword())) {
			user.setPassword(PasswordEncoder.encode(user.getPassword(), user.getUsername()));
		}
		user.setUsedMemory(null);
		user.setTotalMemory(null);
		user.setUserStatus(null);
		user.setRegTime(null);
		userNetdiskMapper.updateByPrimaryKeySelective(user);
		this.updatePrincipal();
		return result;
	}


	@Override
	public void updatePrincipal() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			// 登录后再更新
			UserNetdisk userNetdisk = (UserNetdisk) subject.getPrincipal();
			UserNetdisk userNetdiskNew = userNetdiskMapper.selectByPrimaryKey(userNetdisk.getId());
			PrincipalCollection principalCollection = subject.getPrincipals();
			String realmName = principalCollection.getRealmNames().iterator().next();
			PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(userNetdiskNew, realmName);
			//重新加载Principal
			subject.runAs(newPrincipalCollection);
		}
	}

	@Override
	public Result uploadAvatar(MultipartFile file) {

		Result result = new Result();
		// 得到上传的文件名称，
		String fileRealName = file.getOriginalFilename();
		// 得到上传文件的扩展名
		String fileExtName = "";
		if (fileRealName.contains(".")) {
			fileExtName = fileRealName.substring(fileRealName.lastIndexOf(".") + 1);
		}
		try(BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024)) {
			byte[] temp = new byte[1024];
			int len = -1;
			while((len = bis.read(temp)) != -1) {
				baos.write(temp, 0, len);
			}
			String filePath = fastDFSClientWrapper.uploadFile(file);
			result.setData(appConfiguration.getFdfsNginxServer() + "/" + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Result getInfo() {
		Result result = new Result();
		UserNetdisk userNetdisk = (UserNetdisk) SecurityUtils.getSubject().getPrincipal();
		if (userNetdisk == null) {
			result.setCode(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT));
			return result;
		}
		long totalMemory = Long.valueOf(userNetdisk.getTotalMemory());
		long usedMemory = Long.valueOf(userNetdisk.getUsedMemory());
		long availableMemory = totalMemory - usedMemory;

		List<Message> messages = messageMapper.findAll(userNetdisk.getId());

		UserNetdiskShowInfo user = new UserNetdiskShowInfo();
		user.setId(userNetdisk.getId());
		user.setAvatar(userNetdisk.getAvatar());
		if (StringUtils.isAnyEmpty(userNetdisk.getRealName())) {
			user.setName(userNetdisk.getUsername());
		} else {
			user.setName(userNetdisk.getRealName());
		}
		user.setTotalMemory(totalMemory);
		user.setUsedMemory(usedMemory);
		user.setAvailableMemory(availableMemory);
		user.setToken(session.getId());
		user.setMessages(messages);

        List<UserFriendListShow> friendList = userFriendListMapper.getFriendList(userNetdisk.getId());
        user.setFriendList(friendList);
        result.setData(user);
		return result;
	}

	@Override
	public Result login(UserNetdisk userNetdisk) {
		Result result = new Result();
		//判断验证码
		String imgCodeRedis = (String) redisUtils.get(appConfiguration.getCaptchaPrefix() + session.getId());
		if (StringUtils.isEmpty(imgCodeRedis)) {
			result.setCode(NetdiskErrMsgConstant.LOGIN_CAPTCHA_TIMEOUT);
			return result;
		}
		if(!imgCodeRedis.equalsIgnoreCase(userNetdisk.getImgCode())) {
			result.setCode(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR));
			return result;
		}
		//获取当前登录对象
		Subject currentUser = SecurityUtils.getSubject();
		//判断是否登陆
		if(!currentUser.isAuthenticated()) {
			UsernamePasswordToken token =
					new UsernamePasswordToken(userNetdisk.getLoginInfo(), userNetdisk.getPassword());
			try {
				if (userNetdisk.getRemindPwd()!=null) {
					token.setRememberMe(true);
				}
				//进行登陆
				currentUser.login(token);

			} catch (UnknownAccountException uae) {//未知用户名
				result.setCode(NetdiskErrMsgConstant.LOGIN_ACCOUNT_ERR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_ACCOUNT_ERR));
				return result;
			} catch (IncorrectCredentialsException ice) {//密码错误
				result.setCode(NetdiskErrMsgConstant.LOGIN_PASSSWORD_ERR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_PASSSWORD_ERR));
				return result;
			} catch (UserRepeatLoginException e) {
				result.setCode(NetdiskErrMsgConstant.LOGIN_USER_REPEAT_LOGIN_ERR);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_USER_REPEAT_LOGIN_ERR));
				return result;
			} catch (AuthenticationException ae) {//所有认证异常的父异常
				result.setCode(NetdiskErrMsgConstant.LOGIN_FAILED);
				result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_FAILED));
				return result;
			}
		}
		UserNetdisk userNow = (UserNetdisk)SecurityUtils.getSubject().getPrincipal();
		Long timeOut = appConfiguration.getGlobalSessionTimeout().longValue();
		redisUtils.set(session.getId(), userNow, timeOut);

		UserNetdiskShowInfo userNetdiskShowInfo = (UserNetdiskShowInfo) this.getInfo().getData();

		result.setData(userNetdiskShowInfo);
		return result;
	}

	/**
	 * 用户APP注册
	 * @param param
	 * @return
	 */
	@Transactional
	@Override
	public Result registerApp(UserAppRegisteParam param) {

		Result result = new Result();

		// 未加密原始密码
		String noencodePassword = "";
		if (StringUtils.isAnyEmpty(param.getEmail(), param.getPassword(), param.getImgCode())) {
			return Result.paramIsNull(result);
		}
		noencodePassword = param.getPassword();
		//判断验证码
		String imgCodeRedis = (String) redisUtils.get(appConfiguration.getCaptchaPrefix() + session.getId());
		if (StringUtils.isEmpty(imgCodeRedis)) {
			result.setCode(NetdiskErrMsgConstant.REGISTER_CAPTCHA_TIMEOUT);
			return result;
		}
		if(!imgCodeRedis.equalsIgnoreCase(param.getImgCode())) {
			result.setCode(NetdiskErrMsgConstant.REGISTER_IMG_CODE_ERR);
			return result;
		}

		if (this.findByEmail(param.getEmail()) != null) {
			result.setCode(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST));
			return result;
		}

		Date regTime = new Date();
		UserNetdisk userNetdisk = new UserNetdisk();
		userNetdisk.setUsername(param.getEmail());
		userNetdisk.setEmail(param.getEmail());
		userNetdisk.setRegTime(regTime);
		userNetdisk.setTotalMemory(appConfiguration.getCustomConfiguration().getTotalMemory());
		userNetdisk.setUsedMemory("0");
		userNetdisk.setUserStatus(NetdiskConstant.USER_STATUS_NOT_VERIFY + "");
		userNetdisk.setAvatar(appConfiguration.getCustomConfiguration().getServerHost() + "/logo/default_avatar.gif");

		String key = UUID.randomUUID().toString();
//		String key = "5af009c7-ac31-4f4c-b4d1-43c4acbf415d";
		EmailSendUtil.regNetDisk(userNetdisk.getEmail(), param.getEmail(), key);

		userNetdisk.setPassword(PasswordEncoder.encode(param.getPassword(), param.getEmail()));
		userNetdiskMapper.insertSelective(userNetdisk);

		// 添加消息
		Message message = new Message();
		message.setType(NetdiskConstant.MESSAGE_TYPE_OF_WARNING);
		message.setTitle("账户激活");
		message.setDescription("您的账户尚未激活，激活链接已经发送到您邮箱，请尽快激活");
		message.setUserId(userNetdisk.getId());
		messageMapper.insertSelective(message);

		message.setType(NetdiskConstant.MESSAGE_TYPE_OF_SUCCESS);
		message.setTitle("注册成功");
		message.setDescription("恭喜您注册成功，网盘初始容量为" + new Double(appConfiguration.getCustomConfiguration().getTotalMemory()) / (1024*1024) + "M");
		messageMapper.insertSelective(message);

		Map<String, Object> map = new HashMap<>();
		map.put("key", key);
		map.put("id", userNetdisk.getId());
		redisUtils.set("EMAIL_KEY:" + key, map, appConfiguration.getCustomConfiguration().getVerifyEmailExpire().longValue());

		// 注册完立即登录
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token =
				new UsernamePasswordToken(param.getEmail(), noencodePassword);
		currentUser.login(token);

		UserNetdiskShowInfo userNetdiskShowInfo = (UserNetdiskShowInfo) this.getInfo().getData();
		session.setAttribute("userId", userNetdiskShowInfo.getId());
		result.setData(userNetdiskShowInfo);

		return result;
	}

	@Override
	public Result checkImgCode(String imgCode, HttpSession session) {

		Result result = new Result();
		String imgCodeFromRedis = (String) redisUtils.get(appConfiguration.getCaptchaPrefix() + session.getId());
		if(!imgCodeFromRedis.equalsIgnoreCase(imgCode)) {
			result.setData(NetdiskErrMsgConstant.REGISTER_IMG_CODE_ERR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.REGISTER_IMG_CODE_ERR));
		}
		return result;
	}

	@Override
	public void createImg(HttpServletRequest request, HttpServletResponse response) {

		Object[] objs = ImgCodeUtil.createImage();
		String imgCode = objs[1].toString();
		HttpSession session = request.getSession();
		redisUtils.set(appConfiguration.getCaptchaPrefix() + session.getId(), imgCode, appConfiguration.getCaptchaTimeout());

		BufferedImage bufferedImage = (BufferedImage) objs[0];
		response.setContentType("image/png");
		ServletOutputStream output = null;
		try {
			output = response.getOutputStream();
			ImageIO.write(bufferedImage, "png", output);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (output!=null) {
					output.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
