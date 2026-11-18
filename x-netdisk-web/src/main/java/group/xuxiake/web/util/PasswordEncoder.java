package group.xuxiake.web.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.mindrot.jbcrypt.BCrypt;

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
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}

	/**
	 * 验证用户输入密码是否正确
	 * @param pwdInput 输入的原始密码
	 * @param pwdTrue 加密后的正确密码
	 * @param username 用户名
	 * @return 用户输入密码是否正确
	 */
	public static boolean isPasswordValid(String pwdInput, String pwdTrue, String username) {
		if (pwdInput == null || pwdTrue == null) {
			return false;
		}
		if (pwdTrue.startsWith("$2a$") || pwdTrue.startsWith("$2b$") || pwdTrue.startsWith("$2y$")) {
			return BCrypt.checkpw(pwdInput, pwdTrue);
		}
		return pwdTrue.equals(legacyEncode(pwdInput, username));
	}

	public static boolean needsUpgrade(String encodedPassword) {
		return encodedPassword != null && !encodedPassword.startsWith("$2");
	}

	private static String legacyEncode(String password, String username) {
		return new SimpleHash("MD5", password, username, 1024).toString();
	}
}
