package group.xuxiake.web.util;

import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * 密码加密及验证密码
 */
public class PasswordEncoder {

	/**
	 * 密码加密
	 * @param password 原始密码
	 * @param username 用户名
	 * @return 加密后的密码
	 */
	public static String encode(String password, String username) {
		//加密方式
		String algorithmName = "MD5";
		//加密次数
		int hashIterations = 1024;
		//盐值
		String salt = username;
		//加密后的密码
		SimpleHash simpleHash = new SimpleHash(algorithmName, password, salt, hashIterations );
		return simpleHash.toString();
	}

	/**
	 * 验证用户输入密码是否正确
	 * @param pwdInput 输入的原始密码
	 * @param pwdTrue 加密后的正确密码
	 * @param username 用户名
	 * @return 用户输入密码是否正确
	 */
	public static boolean isPasswordValid(String pwdInput, String pwdTrue, String username) {
		if (pwdTrue.equals(encode(pwdInput, username))) {
			return true;
		}else {
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println(PasswordEncoder.encode("123456", "xuxiake"));
	}
}
