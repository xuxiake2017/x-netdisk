package group.xuxiake.web.util;

import java.security.SecureRandom;

/**
 * 产生随机码的工具类
 * @author 13155
 *
 */
public class RandomCodeUtil {
	private static final SecureRandom RANDOM = new SecureRandom();

	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'q', 'w', 'e', 'r', 't',
			'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm',
			'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X',
			'C', 'V', 'B', 'N', 'M' };
	/**
	 * 产生注册短信、邮箱验证码
	 * @return
	 */
	public static String getRandomCode(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			int randomNum = RANDOM.nextInt(10);
			sb.append(randomNum);
		}
		return sb.toString();
	}
	/**
	 * 产生随机分享id
	 * @return
	 */
	public static String getRandomShareId(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			int randomNum = RANDOM.nextInt(CHARS.length);
			sb.append(CHARS[randomNum]);
		}
		return sb.toString();
	}
	/**
	 * 产生随机分享密码
	 * @return
	 */
	public static String getRandomSharePwd(){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			int randomNum = RANDOM.nextInt(CHARS.length);
			sb.append(CHARS[randomNum]);
		}
		return sb.toString();
	}
}
