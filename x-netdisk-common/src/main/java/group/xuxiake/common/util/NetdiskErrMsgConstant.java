package group.xuxiake.common.util;

import java.util.HashMap;
import java.util.Map;

public class NetdiskErrMsgConstant {

	public static Map<Integer, String> errMessage = null;
	static {
		errMessage = new HashMap<>();
		errMessage.put(NetdiskErrMsgConstant.EXCEPTION, "程序错误");
		errMessage.put(NetdiskErrMsgConstant.UN_AUTHENTICATED, "未授权");
		errMessage.put(NetdiskErrMsgConstant.PARAM_IS_NULL, "参数为空");
		errMessage.put(NetdiskErrMsgConstant.USER_IS_NOT_VERIFY, "账户未验证激活");

		errMessage.put(NetdiskErrMsgConstant.LOGIN_IMG_CODE_ERR, "验证码错误");
		errMessage.put(NetdiskErrMsgConstant.LOGIN_ACCOUNT_ERR, "账号不存在");
		errMessage.put(NetdiskErrMsgConstant.LOGIN_PASSSWORD_ERR, "密码错误");
		errMessage.put(NetdiskErrMsgConstant.LOGIN_CAPTCHA_TIMEOUT, "验证码超时");
		errMessage.put(NetdiskErrMsgConstant.LOGIN_USER_REPEAT_LOGIN_ERR, "您的账户已经登录");
		errMessage.put(NetdiskErrMsgConstant.LOGIN_FAILED, "登录失败");

		errMessage.put(NetdiskErrMsgConstant.GET_INFO_SESSION_TIME_OUT, "登录过期");

		errMessage.put(NetdiskErrMsgConstant.UPLOAD_FILE_IS_NULL, "文件为空");
		errMessage.put(NetdiskErrMsgConstant.UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH, "剩余空间不足，请删除部分文件再试");
		errMessage.put(NetdiskErrMsgConstant.UPLOAD_TIMEOUT, "上传超时");
		errMessage.put(NetdiskErrMsgConstant.UPLOAD_FAILED, "上传失败");
		
		errMessage.put(NetdiskErrMsgConstant.CHECKMD5_OUT_OF_MEMORY_LIMIT, "单个文件超出最大值");
		errMessage.put(NetdiskErrMsgConstant.CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH, "剩余空间不足");
		errMessage.put(NetdiskErrMsgConstant.CHECKMD5_MD5_NOT_EXIST, "服务器不存在该MD5值，需要上传");
		errMessage.put(NetdiskErrMsgConstant.CHECKMD5_MD5_EXIST, "服务器存在该MD5值");
		errMessage.put(NetdiskErrMsgConstant.CHECKMD5_FAILED, "检查MD5值失败");
		
		errMessage.put(NetdiskErrMsgConstant.MKDIR_NAME_IS_NULL, "文件夹名为空");
		errMessage.put(NetdiskErrMsgConstant.MKDIR_NAME_EXIST, "该目录下已存在同名文件夹");
		errMessage.put(NetdiskErrMsgConstant.MKDIR_FAILED, "新建失败");
		
		errMessage.put(NetdiskErrMsgConstant.MOVEFILE_FAILED, "移动失败");
		errMessage.put(NetdiskErrMsgConstant.MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR, "移动失败，目标目录存在同名文件夹");

		errMessage.put(NetdiskErrMsgConstant.RENAME_FILE_NAME_EXIST, "该目录下已存在同名文件夹");
		errMessage.put(NetdiskErrMsgConstant.RENAME_FILE_FAILED, "重命名失败");

		errMessage.put(NetdiskErrMsgConstant.REGISTER_USERNAME_EXIST, "用户名已被注册");
		errMessage.put(NetdiskErrMsgConstant.REGISTER_PHONE_EXIST, "电话已被注册");
		errMessage.put(NetdiskErrMsgConstant.REGISTER_EMAIL_EXIST, "邮箱已被注册");
		errMessage.put(NetdiskErrMsgConstant.REGISTER_IMG_CODE_ERR, "验证码错误");
		errMessage.put(NetdiskErrMsgConstant.REGISTER_CAPTCHA_TIMEOUT, "验证码超时");

		errMessage.put(NetdiskErrMsgConstant.SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL, "您操作太频繁了，请稍后再试");
		errMessage.put(NetdiskErrMsgConstant.SEND_SMS_CODE_FAILED, "抱歉，验证码发送失败");

		errMessage.put(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_TIME_OUT, "短信验证码超时，请重新发送");
		errMessage.put(NetdiskErrMsgConstant.USER_REGISTER_SMS_CODE_ERR, "短信验证码错误");

		errMessage.put(NetdiskErrMsgConstant.VERIFY_TIME_OUT, "验证链接已经过期");

		errMessage.put(NetdiskErrMsgConstant.DELETE_FILE_FAILED, "文件删除失败");

		errMessage.put(NetdiskErrMsgConstant.FILE_RESTORE_FAILED, "文件恢复失败");
		errMessage.put(NetdiskErrMsgConstant.FILE_RESTORE_MAKE_RESOURCES_DIR, "父目录不存在，文件已被恢复到“我的资源”文件夹");
		errMessage.put(NetdiskErrMsgConstant.FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR, "目标目录存在同名文件夹，文件恢复失败");
		errMessage.put(NetdiskErrMsgConstant.FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH, "文件恢复失败，剩余空间不足，请删除部分文件再试");

		errMessage.put(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREID_NOT_EXIST, "您所访问的资源不存在");
		errMessage.put(NetdiskErrMsgConstant.GET_SHARE_FILE_SHAREPWD_IS_WRONG, "分享密码错误");

		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_IS_NOT_AUTHENTICATED, "请先登录");
		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH, "您的剩余空间不足，请删除部分文件再试！");
		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR, "保存失败，目标目录存在同名文件夹");
		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_SAVE_TO_CLOUD_FAILED, "保存失败");

		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_DOWNLOAD_WRONG, "下载出错");

		errMessage.put(NetdiskErrMsgConstant.SHARE_FILE_GET_FILE_LIST_WRONG, "获取文件列表错误");

		errMessage.put(NetdiskErrMsgConstant.ADD_FRIEND_REQUEST_FRIEND_IS_EXIST, "对方已是您的好友");

		errMessage.put(NetdiskErrMsgConstant.UN_AVAILABLE_CHAT_SERVER, "无可用的chat server");
		errMessage.put(NetdiskErrMsgConstant.GET_CHAT_SERVER_AND_TOKEN_IS_NULL, "token为空");
		errMessage.put(NetdiskErrMsgConstant.UN_AVAILABLE_ROUTE_SERVER, "无可用的route server");
	}
	public static String getErrMessage(Integer errCode) {

		if (errCode != null){
			if (errMessage.get(errCode) != null) {
				return errMessage.get(errCode);
			}
		}
		return null;
	}
	/*通用请求成功*/
	public static final int REQUEST_SUCCESS = 20000;
	/*参数为空*/
	public static final int PARAM_IS_NULL = 30000;
	/*程序异常*/
	public static final int EXCEPTION = 50000;
	/*未授权*/
	public static final int UN_AUTHENTICATED = 41000;
	/*账户未验证*/
	public static final int USER_IS_NOT_VERIFY = 42000;
	
	/*登录-验证码错误*/
	public static final int LOGIN_IMG_CODE_ERR = 20010;
	/*登录-账号不存在、错误*/
	public static final int LOGIN_ACCOUNT_ERR = 20011;
	/*登录-密码错误*/
	public static final int LOGIN_PASSSWORD_ERR = 20012;
	/*登录-验证码超时*/
	public static final int LOGIN_CAPTCHA_TIMEOUT = 20013;
	/*登录-用户重复登录*/
	public static final int LOGIN_USER_REPEAT_LOGIN_ERR = 20014;
	/*登录-登录失败*/
	public static final int LOGIN_FAILED = 20015;
	/*获取用户信息-登录过期*/
	public static final int GET_INFO_SESSION_TIME_OUT = 20016;
	
	/*文件上传-文件为空*/
	public static final int UPLOAD_FILE_IS_NULL = 20021;
	/*文件上传-剩余空间不足*/
	public static final int UPLOAD_AVAIABLE_MEMORY_NOT_ENOUGH = 20022;
	/*文件上传-上传超时*/
	public static final int UPLOAD_TIMEOUT = 20023;
	/*文件上传-上传失败*/
	public static final int UPLOAD_FAILED = 20024;
	
	/*检查MD5-单个文件超出最大值！*/
	public static final int CHECKMD5_OUT_OF_MEMORY_LIMIT = 20031;
	/*检查MD5-剩余空间不足*/
	public static final int CHECKMD5_AVAIABLE_MEMORY_NOT_ENOUGH = 20032;
	/*检查MD5-服务器不存在该MD5值，需要上传(非错误信息，前端需要特殊识别)*/
	public static final int CHECKMD5_MD5_NOT_EXIST = 20033;
	/*检查MD5-服务器存在该MD5值*/
	public static final int CHECKMD5_MD5_EXIST = 20034;
	/*检查MD5-检查失败*/
	public static final int CHECKMD5_FAILED = 20035;
	
	/*新建文件夹-文件夹名为空*/
	public static final int MKDIR_NAME_IS_NULL = 20041;
	/*新建文件夹-该目录下已存在同名文件夹*/
	public static final int MKDIR_NAME_EXIST = 20042;
	/*新建文件夹-新建失败*/
	public static final int MKDIR_FAILED = 20043;
	
	/*移动文件-移动失败*/
	public static final int MOVEFILE_FAILED = 20051;
	/*移动文件-目标目录存在同名文件夹*/
	public static final int MOVEFILE_TARGET_DIR_EXIST_SAME_NAME_DIR = 20052;

	/*重命名文件-当前目录下有同名文件夹*/
	public static final int RENAME_FILE_NAME_EXIST = 20061;
	/*重命名文件-重命名失败*/
	public static final int RENAME_FILE_FAILED = 20062;

	/*注册-用户名已被注册*/
	public static final int REGISTER_USERNAME_EXIST = 20071;
	/*注册-电话已被注册*/
	public static final int REGISTER_PHONE_EXIST = 20072;
	/*注册-邮箱已被注册*/
	public static final int REGISTER_EMAIL_EXIST = 20073;
	/*注册-验证码错误*/
	public static final int REGISTER_IMG_CODE_ERR = 20074;
	/*注册-验证码超时*/
	public static final int REGISTER_CAPTCHA_TIMEOUT = 20075;

	/*发送短信验证码-操作太频繁*/
	public static final int SEND_SMS_CODE_BUSINESS_LIMIT_CONTROL = 20081;
	/*发送短信验证码-发送失败*/
	public static final int SEND_SMS_CODE_FAILED = 20082;

	/*用户注册-短信验证码超时*/
	public static final int USER_REGISTER_SMS_CODE_TIME_OUT = 20091;
	/*用户注册-短信验证码错误*/
	public static final int USER_REGISTER_SMS_CODE_ERR = 20092;

	/*邮箱验证-超时*/
	public static final int VERIFY_TIME_OUT = 20101;
	/*邮箱验证-已验证*/
	public static final int VERIFY_HAVING_BEEN_VERIFIED = 20102;

	/*文件删除-失败*/
	public static final int DELETE_FILE_FAILED = 20111;

	/*文件恢复-失败*/
	public static final int FILE_RESTORE_FAILED = 20121;
	/*文件恢复-创建"我的资源"文件夹*/
	public static final int FILE_RESTORE_MAKE_RESOURCES_DIR = 20122;
	/*文件恢复-目标目录存在同名文件夹，文件恢复失败*/
	public static final int FILE_RESTORE_TARGET_DIR_EXIST_SAME_NAME_DIR = 20123;
	/*文件恢复-剩余空间不足，文件恢复失败*/
	public static final int FILE_RESTORE_AVAILABLEMEMORY_NOT_ENOUGH = 20124;

	/*提取分享文件-shareId不存在*/
	public static final int GET_SHARE_FILE_SHAREID_NOT_EXIST = 20131;
	/*提取分享文件-分享密码错误*/
	public static final int GET_SHARE_FILE_SHAREPWD_IS_WRONG = 20132;

	/*保存分享文件到网盘-未登录*/
	public static final int SHARE_FILE_SAVE_TO_CLOUD_IS_NOT_AUTHENTICATED = 20141;
	/*保存分享文件到网盘-剩余空间不足*/
	public static final int SHARE_FILE_SAVE_TO_CLOUD_AVAIABLE_MEMORY_NOT_ENOUGH = 20142;
	/*保存分享文件到网盘-目标目录存在同名文件夹*/
	public static final int SHARE_FILE_SAVE_TO_CLOUD_TARGET_DIR_EXIST_SAME_NAME_DIR = 20143;
	/*保存分享文件到网盘-保存失败*/
	public static final int SHARE_FILE_SAVE_TO_CLOUD_FAILED = 20144;

	/*下载分享文件-错误*/
	public static final int SHARE_FILE_DOWNLOAD_WRONG = 20151;

	/*获取分享文件列表-错误*/
	public static final int SHARE_FILE_GET_FILE_LIST_WRONG = 20161;

	/*发送添加好友请求-好友已存在*/
	public static final int ADD_FRIEND_REQUEST_FRIEND_IS_EXIST = 20171;

	/*无可用的chat server*/
	public static final int UN_AVAILABLE_CHAT_SERVER = 20181;
	/*token为空*/
	public static final int GET_CHAT_SERVER_AND_TOKEN_IS_NULL = 20182;
	/*无可用的route server*/
	public static final int UN_AVAILABLE_ROUTE_SERVER = 20183;

}
