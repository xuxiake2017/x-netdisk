package group.xuxiake.web.service.impl;

import group.xuxiake.common.entity.Result;
import group.xuxiake.common.entity.SysMessage;
import group.xuxiake.common.entity.User;
import group.xuxiake.common.entity.param.UpdatePasswordParam;
import group.xuxiake.common.entity.param.UserLoginParam;
import group.xuxiake.common.entity.param.UserProfileUpdateParam;
import group.xuxiake.common.entity.param.ChangeEmailParam;
import group.xuxiake.common.entity.param.ChangePhoneParam;
import group.xuxiake.common.enums.ClientType;
import group.xuxiake.common.enums.LogType;
import group.xuxiake.common.mapper.SysMessageMapper;
import group.xuxiake.common.mapper.UserMapper;
import group.xuxiake.common.mapper.StorageDeleteTaskMapper;
import group.xuxiake.common.util.*;
import group.xuxiake.web.aspect.SysLogRecord;
import group.xuxiake.web.configuration.AppConfiguration;
import group.xuxiake.web.configuration.CustomConfiguration;
import group.xuxiake.common.entity.param.UserAppRegisteParam;
import group.xuxiake.common.entity.show.UserFriendListShow;
import group.xuxiake.common.entity.show.UserNetdiskShowInfo;
import group.xuxiake.web.exception.UserRepeatLoginException;
import group.xuxiake.common.mapper.UserFriendListMapper;
import group.xuxiake.web.service.UserService;
import group.xuxiake.web.service.VerifyService;
import group.xuxiake.web.shiro.AutoLoginToken;
import group.xuxiake.web.shiro.LoginType;
import group.xuxiake.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.session.Session;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.vdurmont.emoji.EmojiParser;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.*;

@Slf4j
@Service("userNetdiskService")
public class UserServiceImpl implements UserService {

	@Resource
	private UserMapper userMapper;
	@Autowired
	private HttpSession session;
	@Resource
	private SysMessageMapper messageMapper;
	@Resource
	private UserFriendListMapper userFriendListMapper;
	@Resource
	private FastDFSClientWrapper fastDFSClientWrapper;
	@Resource
	private AppConfiguration appConfiguration;
	@Resource
	private RedisUtils redisUtils;
	@Resource
	private VerifyService verifyService;
	@Resource
	private RedisSessionDAO redisSessionDAO;
	@Resource
	private StorageDeleteTaskMapper storageDeleteTaskMapper;
	@Override
	public User findByName(String userName) {
		User user = new User();
		user.setUsername(userName);
		return userMapper.findByLoginInfo(user);
	}

	@Override
	public User findByPhone(String phone) {
		User user = new User();
		user.setPhone(phone);
		return userMapper.findByLoginInfo(user);
	}

	@Override
	public User findByEmail(String email) {
		User user = new User();
		user.setEmail(email);
		return userMapper.findByLoginInfo(user);
	}

	@SysLogRecord(logType = LogType.REGISTER, clientType = ClientType.WEB)
	@Transactional
	@Override
	public Result register(UserAppRegisteParam param) {
		return registerByEmailCode(param);
	}

	@Override
	public User findById(Integer id) {
		return userMapper.selectByPrimaryKey(id);
	}

	@Override
	public Integer updateUser(User user) {
		return userMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public Result detail() {
		Result result = new Result();
		User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
		if (userThreadContext == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
		User user = userMapper.selectByPrimaryKey(userThreadContext.getId());
		if (user == null) {
			result.setCode(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT);
			return result;
		}
		user.setPassword(null);
		user.setNickName(EmojiParser.parseToUnicode(user.getNickName()));
		user.setSignature(EmojiParser.parseToUnicode(user.getSignature()));
		result.setData(user);
		return result;
	}

	@Transactional
	@Override
	public Result update(UserProfileUpdateParam param) {
		Result result = new Result();
		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
		if (!isValidProfile(param)) {
			result.setCode(NetdiskErrMsgConstant.USER_PROFILE_INVALID);
			return result;
		}
		User currentUser = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
		if (currentUser == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
		User update = new User();
		update.setId(currentUser.getId());
		update.setSex(param.getSex());
		if (param.getRealName() != null) {
			update.setRealName(param.getRealName().trim());
		}
		if (param.getSignature() != null) {
			update.setSignature(EmojiParser.parseToAliases(param.getSignature().trim()));
		}
		if (param.getNickName() != null) {
			update.setNickName(EmojiParser.parseToAliases(param.getNickName().trim()));
		}
		userMapper.updateByPrimaryKeySelective(update);
		refreshPrincipal(currentUser.getId());
		result.setData(safeUser(userMapper.selectByPrimaryKey(currentUser.getId())));
		return result;
	}

	private void refreshPrincipal(Integer userId) {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated() && subject.getPrincipals() != null) {
			User userNew = userMapper.selectByPrimaryKey(userId);
			if (userNew == null) {
				return;
			}
			userNew.setPassword(null);
			Iterator<String> realmNames = subject.getPrincipals().getRealmNames().iterator();
			String realmName = realmNames.hasNext() ? realmNames.next() : "userRealm";
			SimplePrincipalCollection principals = new SimplePrincipalCollection(userNew, realmName);
			subject.getSession().setAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY, principals);
			redisUtils.set(session.getId(), userNew, appConfiguration.getGlobalSessionTimeout().longValue());
		}
	}

	@Override
	public Result updatePrincipal() {
		User principal = currentPrincipal();
		if (principal == null) {
			return unauthorized();
		}
		refreshPrincipal(principal.getId());
		return new Result();
	}

	private User safeUser(User user) {
		if (user != null) {
			user.setPassword(null);
			user.setNickName(EmojiParser.parseToUnicode(user.getNickName()));
			user.setSignature(EmojiParser.parseToUnicode(user.getSignature()));
		}
		return user;
	}

	@Transactional
	@Override
	public Result uploadAvatar(MultipartFile file) {
		Result result = new Result();
		User principal = (User) SecurityUtils.getSubject().getPrincipal();
		if (principal == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
		String avatarExtension = inspectAvatar(file);
		if (avatarExtension == null) {
			result.setCode(NetdiskErrMsgConstant.USER_AVATAR_INVALID);
			return result;
		}
		String uploadedPath = null;
		try {
			try (InputStream input = file.getInputStream()) {
				uploadedPath = fastDFSClientWrapper.uploadFile(input, file.getSize(), avatarExtension);
			}
			String avatarUrl = appConfiguration.getFdfsNginxServer() + "/" + uploadedPath;
			User current = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
			if (current == null) {
				throw new IllegalStateException("用户不存在");
			}
			String oldAvatar = current.getAvatar();
			User update = new User();
			update.setId(current.getId());
			update.setAvatar(avatarUrl);
			if (userMapper.updateByPrimaryKeySelective(update) != 1) {
				throw new IllegalStateException("头像更新失败");
			}
			String oldStoragePath = managedStoragePath(oldAvatar);
			if (oldStoragePath != null && !avatarUrl.equals(oldAvatar)
					&& userMapper.countByAvatar(oldAvatar) == 0) {
				storageDeleteTaskMapper.insertIgnore(oldStoragePath);
				deleteOldAvatarAfterCommit(oldStoragePath);
			}
			refreshPrincipal(current.getId());
			result.setData(avatarUrl);
			return result;
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			if (uploadedPath != null) {
				try {
					fastDFSClientWrapper.deleteFile(uploadedPath);
				} catch (RuntimeException deleteError) {
					log.error("清理上传失败的头像异常: {}", uploadedPath, deleteError);
				}
			}
			log.error("头像修改失败", e);
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("头像修改失败");
		}
		return result;
	}

	@Override
	public Result getInfo() {
		Result result = new Result();
		User userThreadContext = (User) SecurityUtils.getSubject().getPrincipal();
		if (userThreadContext == null) {
			result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
			return result;
		}
		User user = userMapper.selectByPrimaryKey(userThreadContext.getId());
		if (user == null) {
			result.setCode(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT));
			return result;
		}
		long totalMemory = user.getTotalMemory();
		long usedMemory = user.getUsedMemory();
		long availableMemory = totalMemory - usedMemory;

		List<SysMessage> messages = messageMapper.findAll(user.getId());

		UserNetdiskShowInfo userInfo = new UserNetdiskShowInfo();
		userInfo.setId(user.getId());
		userInfo.setAvatar(user.getAvatar());
		userInfo.setName(EmojiParser.parseToUnicode(user.getNickName()));
		userInfo.setTotalMemory(totalMemory);
		userInfo.setUsedMemory(usedMemory);
		userInfo.setAvailableMemory(availableMemory);
		userInfo.setToken(session.getId());
		userInfo.setMessages(messages);

        List<UserFriendListShow> friendList = userFriendListMapper.getFriendList(user.getId());
		userInfo.setFriendList(friendList);
        result.setData(userInfo);
		return result;
	}

	@SysLogRecord(logType = LogType.LOGIN)
	@Override
	public Result login(UserLoginParam param) {
		Result result = new Result();
		//判断验证码
		String imgCodeRedis = (String) redisUtils.get(appConfiguration.getCaptchaPrefix() + session.getId());
		if (StringUtils.isEmpty(imgCodeRedis)) {
			result.setCode(NetdiskErrMsgConstant.LOGIN_CAPTCHA_TIMEOUT);
			return result;
		}
		if(!imgCodeRedis.equalsIgnoreCase(param.getImgCode())) {
			result.setCode(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR);
			result.setMsg(NetdiskErrMsgConstant.getErrMessage(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR));
			return result;
		}
		//获取当前登录对象
		Subject currentUser = SecurityUtils.getSubject();
		//判断是否登陆
		if(!currentUser.isAuthenticated()) {
			AutoLoginToken token = new AutoLoginToken(param.getLoginInfo(), param.getPassword(), LoginType.PASSWORD);
			try {
				if (param.getRemindPwd()!=null) {
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
		User userNow = (User)SecurityUtils.getSubject().getPrincipal();
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
	@SysLogRecord(logType = LogType.REGISTER, clientType = ClientType.H5)
	@Transactional
	@Override
	public Result registerApp(UserAppRegisteParam param) {
		return registerByEmailCode(param);
	}

	private Result registerByEmailCode(UserAppRegisteParam param) {
		Result result = new Result();

		if (param == null || StringUtils.isAnyBlank(param.getEmail(), param.getPassword(), param.getEmailCode())) {
			return Result.paramIsNull(result);
		}
		String email = EmailAddressUtil.normalize(param.getEmail());
		if (!EmailAddressUtil.isValid(email)) {
			return Result.paramIsNull(result);
		}
		String noencodePassword = param.getPassword();

		if (this.findByEmail(email) != null) {
			result.setCode(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST);
			return result;
		}

		String codeKey = "EMAIL_REGISTER_CODE:" + email;
		String attemptsKey = "EMAIL_REGISTER_ATTEMPTS:" + email;
		String expectedCode = (String) redisUtils.get(codeKey);
		if (StringUtils.isBlank(expectedCode)) {
			result.setCode(NetdiskErrMsgConstant.USER_REGISTER_EMAIL_CODE_TIME_OUT);
			return result;
		}
		long attempts = redisUtils.incr(attemptsKey, 1L);
		if (attempts == 1L) {
			long expireSeconds = Math.max(1L,
					appConfiguration.getCustomConfiguration().getEmailCodeExpire() / 1000L);
			redisUtils.expire(attemptsKey, expireSeconds);
		}
		if (attempts > appConfiguration.getCustomConfiguration().getEmailMaxVerifyAttempts()) {
			redisUtils.del(codeKey);
			result.setCode(NetdiskErrMsgConstant.USER_REGISTER_EMAIL_CODE_ATTEMPTS_EXCEEDED);
			return result;
		}
		String submittedCode = param.getEmailCode().trim();
		if (!expectedCode.equals(submittedCode)) {
			if (attempts >= appConfiguration.getCustomConfiguration().getEmailMaxVerifyAttempts()) {
				redisUtils.del(codeKey);
				result.setCode(NetdiskErrMsgConstant.USER_REGISTER_EMAIL_CODE_ATTEMPTS_EXCEEDED);
			} else {
				result.setCode(NetdiskErrMsgConstant.USER_REGISTER_EMAIL_CODE_ERR);
			}
			return result;
		}
		if (!redisUtils.compareAndDelete(codeKey, submittedCode)) {
			result.setCode(NetdiskErrMsgConstant.USER_REGISTER_EMAIL_CODE_TIME_OUT);
			return result;
		}
		redisUtils.del(attemptsKey);

		Date regTime = new Date();
		User user = new User();
		user.setUsername(email);
		user.setEmail(email);
		user.setRegTime(regTime);
		user.setTotalMemory(appConfiguration.getCustomConfiguration().getTotalMemory());
		user.setUsedMemory(0L);
		user.setUserStatus(NetdiskConstant.USER_STATUS_NORMAL);
		user.setNickName(NicknameGenerator.generate());

		user.setPassword(PasswordEncoder.encode(param.getPassword(), email));
		try {
			userMapper.insertSelective(user);
		} catch (DuplicateKeyException e) {
			result.setCode(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST);
			return result;
		}

		SysMessage message = new SysMessage();
		message.setType(NetdiskConstant.MESSAGE_TYPE_OF_SUCCESS);
		message.setTitle("注册成功");
		message.setDescription("恭喜您注册成功，网盘初始容量为" + new Double(appConfiguration.getCustomConfiguration().getTotalMemory()) / (1024*1024) + "M");
		message.setUserId(user.getId());
		messageMapper.insertSelective(message);

		// 注册完立即登录
		Subject currentUser = SecurityUtils.getSubject();
		AutoLoginToken token = new AutoLoginToken(email, noencodePassword, LoginType.PASSWORD);
		currentUser.login(token);

		User userNow = (User)SecurityUtils.getSubject().getPrincipal();
		Long timeOut = appConfiguration.getGlobalSessionTimeout().longValue();
		redisUtils.set(session.getId(), userNow, timeOut);

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

		Object[] objs = group.xuxiake.common.util.ImgCodeUtil.createImage();
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

	@Transactional
	@Override
	public Result updatePassword(UpdatePasswordParam param) {
		Result result = new Result();
		try {
			if (param == null || StringUtils.isAnyEmpty(param.getOldPassword(), param.getNewPassword(), param.getRepeatPassword())) {
				result.setCode(NetdiskErrMsgConstant.PARAM_IS_NULL);
				return result;
			}

			PrivateKey privateKey = RSAUtil.getPrivateKey("private.key");
			String oldPassword = RSAUtil.decryptText(param.getOldPassword(), privateKey);
			String newPassword = RSAUtil.decryptText(param.getNewPassword(), privateKey);
			String repeatPassword = RSAUtil.decryptText(param.getRepeatPassword(), privateKey);
			if (newPassword == null || repeatPassword == null || !newPassword.equals(repeatPassword)) {
				result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
				result.setMsg("两次输入密码不一致");
				return result;
			}
			if (!isStrongPassword(newPassword)) {
				result.setCode(NetdiskErrMsgConstant.USER_PASSWORD_WEAK);
				return result;
			}

			User principal = (User) SecurityUtils.getSubject().getPrincipal();
			if (principal == null) {
				result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
				return result;
			}
			User current = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
			if (current == null || !PasswordEncoder.isPasswordValid(oldPassword, current.getPassword(), current.getUsername())) {
				result.setCode(NetdiskErrMsgConstant.USER_PASSWORD_OLD_WRONG);
				return result;
			}
			if (PasswordEncoder.isPasswordValid(newPassword, current.getPassword(), current.getUsername())) {
				result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
				result.setMsg("新密码不能与当前密码相同");
				return result;
			}
			User passwordUpdate = new User();
			passwordUpdate.setId(principal.getId());
			passwordUpdate.setPassword(PasswordEncoder.encode(newPassword, current.getUsername()));
			userMapper.updateByPrimaryKeySelective(passwordUpdate);
			invalidateOtherSessions(current.getId());
		} catch (Exception e) {
			log.error("密码修改失败", e);
			result.setCode(NetdiskErrMsgConstant.REQUEST_ERROR);
			result.setMsg("密码修改失败");
			return result;
		}
		result.setMsg("密码修改成功");
		return result;
	}

	@Override
	public Result sendEmailChangeCode(String email) {
		User principal = currentPrincipal();
		return principal == null ? unauthorized() : verifyService.sendEmailChangeCode(principal.getId(), email);
	}

	@Transactional
	@Override
	public Result changeEmail(ChangeEmailParam param) {
		Result result = new Result();
		User principal = currentPrincipal();
		String email = param == null ? null : EmailAddressUtil.normalize(param.getEmail());
		if (principal == null) return unauthorized();
		if (!EmailAddressUtil.isValid(email) || StringUtils.isBlank(param.getEmailCode())) return Result.paramIsNull(result);
		if (!verifyChangeCode("EMAIL_CHANGE", principal.getId(), email, param.getEmailCode(),
				NetdiskErrMsgConstant.USER_EMAIL_CODE_TIMEOUT, NetdiskErrMsgConstant.USER_EMAIL_CODE_WRONG, result)) return result;
		User current = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
		if (current == null) return unauthorized();
		User existing = userMapper.findAnyByEmail(email);
		if (existing != null && !current.getId().equals(existing.getId())) {
			result.setCode(NetdiskErrMsgConstant.USER_EMAIL_EXIST);
			return result;
		}
		User update = new User();
		update.setId(current.getId());
		update.setEmail(email);
		try {
			userMapper.updateByPrimaryKeySelective(update);
		} catch (DuplicateKeyException e) {
			result.setCode(NetdiskErrMsgConstant.USER_EMAIL_EXIST);
			return result;
		}
		refreshPrincipal(current.getId());
		result.setData(safeUser(userMapper.selectByPrimaryKey(current.getId())));
		return result;
	}

	@Override
	public Result sendPhoneChangeCode(String phone) {
		User principal = currentPrincipal();
		return principal == null ? unauthorized() : verifyService.sendPhoneChangeCode(principal.getId(), phone);
	}

	@Transactional
	@Override
	public Result changePhone(ChangePhoneParam param) {
		Result result = new Result();
		User principal = currentPrincipal();
		String phone = param == null ? null : StringUtils.trim(param.getPhone());
		if (principal == null) return unauthorized();
		if (phone == null || !phone.matches("^1\\d{10}$") || StringUtils.isBlank(param.getPhoneCode())) return Result.paramIsNull(result);
		if (!verifyChangeCode("PHONE_CHANGE", principal.getId(), phone, param.getPhoneCode(),
				NetdiskErrMsgConstant.USER_PHONE_CODE_TIMEOUT, NetdiskErrMsgConstant.USER_PHONE_CODE_WRONG, result)) return result;
		User current = userMapper.selectByPrimaryKeyForUpdate(principal.getId());
		if (current == null) return unauthorized();
		User existing = userMapper.findAnyByPhone(phone);
		if (existing != null && !current.getId().equals(existing.getId())) {
			result.setCode(NetdiskErrMsgConstant.USER_PHONE_EXIST);
			return result;
		}
		User update = new User();
		update.setId(current.getId());
		update.setPhone(phone);
		try {
			userMapper.updateByPrimaryKeySelective(update);
		} catch (DuplicateKeyException e) {
			result.setCode(NetdiskErrMsgConstant.USER_PHONE_EXIST);
			return result;
		}
		refreshPrincipal(current.getId());
		result.setData(safeUser(userMapper.selectByPrimaryKey(current.getId())));
		return result;
	}

	private User currentPrincipal() {
		return (User) SecurityUtils.getSubject().getPrincipal();
	}

	private Result unauthorized() {
		Result result = new Result();
		result.setCode(NetdiskErrMsgConstant.UN_AUTHENTICATED);
		return result;
	}

	private boolean verifyChangeCode(String scene, Integer userId, String target, String submitted,
			int timeoutCode, int wrongCode, Result result) {
		String prefix = scene + ":" + userId + ":" + target;
		String codeKey = prefix.replace(scene + ":", scene + "_CODE:");
		String attemptsKey = prefix.replace(scene + ":", scene + "_ATTEMPTS:");
		String expected = (String) redisUtils.get(codeKey);
		if (StringUtils.isBlank(expected)) {
			result.setCode(timeoutCode);
			return false;
		}
		long attempts = redisUtils.incr(attemptsKey, 1L);
		if (attempts == 1L) {
			long ttl = Math.max(1L, scene.startsWith("EMAIL")
					? appConfiguration.getCustomConfiguration().getEmailCodeExpire() / 1000L
					: CustomConfiguration.getVerifySmsExpire() / 1000L);
			redisUtils.expire(attemptsKey, ttl);
		}
		int maxAttempts = appConfiguration.getCustomConfiguration().getEmailMaxVerifyAttempts();
		if (attempts > maxAttempts || !expected.equals(submitted.trim())) {
			if (attempts >= maxAttempts) {
				redisUtils.del(codeKey);
				result.setCode(NetdiskErrMsgConstant.USER_VERIFY_ATTEMPTS_EXCEEDED);
			} else {
				result.setCode(wrongCode);
			}
			return false;
		}
		if (!redisUtils.compareAndDelete(codeKey, submitted.trim())) {
			result.setCode(timeoutCode);
			return false;
		}
		redisUtils.del(attemptsKey);
		return true;
	}

	private boolean isValidProfile(UserProfileUpdateParam param) {
		if (param == null || (param.getSex() == null && param.getRealName() == null
				&& param.getSignature() == null && param.getNickName() == null)) return false;
		if (param.getSex() != null && param.getSex() != 0 && param.getSex() != 1 && param.getSex() != 2) return false;
		return validText(param.getNickName(), 32, false) && validText(param.getRealName(), 50, true)
				&& validText(param.getSignature(), 200, true);
	}

	private boolean validText(String value, int maxCodePoints, boolean allowBlank) {
		if (value == null) return true;
		String text = value.trim();
		if (!allowBlank && text.isEmpty()) return false;
		if (text.codePointCount(0, text.length()) > maxCodePoints) return false;
		for (int i = 0; i < text.length();) {
			int codePoint = text.codePointAt(i);
			if (Character.isISOControl(codePoint)) return false;
			i += Character.charCount(codePoint);
		}
		return true;
	}

	private String inspectAvatar(MultipartFile file) {
		if (file == null || file.isEmpty() || file.getSize() > appConfiguration.getCustomConfiguration().getAvatarSizeMax()) return null;
		ImageReader reader = null;
		try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
			if (input == null) return null;
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) return null;
			reader = readers.next();
			String format = reader.getFormatName().toLowerCase(Locale.ROOT);
			if (!("jpeg".equals(format) || "jpg".equals(format) || "png".equals(format) || "gif".equals(format))) return null;
			reader.setInput(input, true, true);
			int width = reader.getWidth(0);
			int height = reader.getHeight(0);
			if (width < 1 || height < 1 || width > 4096 || height > 4096) return null;
			return "jpeg".equals(format) ? "jpg" : format;
		} catch (IOException | RuntimeException e) {
			return null;
		} finally {
			if (reader != null) reader.dispose();
		}
	}

	private String managedStoragePath(String avatar) {
		if (StringUtils.isBlank(avatar)) return null;
		String prefix = StringUtils.removeEnd(appConfiguration.getFdfsNginxServer(), "/") + "/";
		if (!avatar.startsWith(prefix)) return null;
		String path = avatar.substring(prefix.length());
		return path.indexOf('/') > 0 ? path : null;
	}

	private void deleteOldAvatarAfterCommit(final String storagePath) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					fastDFSClientWrapper.deleteFile(storagePath);
					storageDeleteTaskMapper.deleteByPath(storagePath);
				} catch (RuntimeException e) {
					log.warn("旧头像删除失败，已保留重试任务: {}", storagePath, e);
				}
			}
		});
	}

	private boolean isStrongPassword(String password) {
		return password != null && password.length() >= 8 && password.length() <= 64
				&& password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*");
	}

	private void invalidateOtherSessions(Integer userId) {
		Session currentSession = SecurityUtils.getSubject().getSession(false);
		for (Session active : redisSessionDAO.getActiveSessions()) {
			if (currentSession != null && currentSession.getId().equals(active.getId())) continue;
			Object value = active.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
			if (!(value instanceof SimplePrincipalCollection)) continue;
			Object primary = ((SimplePrincipalCollection) value).getPrimaryPrincipal();
			if (primary instanceof User && userId.equals(((User) primary).getId())) redisSessionDAO.delete(active);
		}
	}
}
