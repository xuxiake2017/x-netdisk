package group.xuxiake.web.shiro;

import org.apache.shiro.crypto.hash.SimpleHash;

public class MD5 {

	public static void main(String[] args) {
		
		String algorithmName = "MD5";
		Object source = "136973";
		Object salt = "821427734@qq.com";
		int hashIterations = 1024;
		SimpleHash simpleHash = new SimpleHash(algorithmName, source, salt, hashIterations );
		System.out.println(simpleHash);
	}
}
