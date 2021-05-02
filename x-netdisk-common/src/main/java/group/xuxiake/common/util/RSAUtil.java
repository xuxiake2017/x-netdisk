package group.xuxiake.common.util;

import com.google.gson.Gson;
import group.xuxiake.common.entity.param.WechatEncryptionParam;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class RSAUtil {

    /**
     * 获取公钥
     * @param filename
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(String filename) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(filename);
        try(InputStream inputStream = classPathResource.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(sb.toString()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(pubX509);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("READ PUBLIC KEY ERROR:", e);
        }
    }

    /**
     * 获取私钥
     * @param filename
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        ClassPathResource classPathResource = new ClassPathResource(filename);
        try(InputStream inputStream = classPathResource.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(sb.toString()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch (Exception e) {
            throw new Exception("READ PRIVATE KEY ERROR:" ,e);
        }
    }

    /**
     * 加密
     * @param msg
     * @param key
     * @return
     */
    public static String encryptText(String msg, PublicKey key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.encodeBase64String(cipher.doFinal(msg.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param msg
     * @param key
     * @return
     */
    public static String decryptText(String msg, PrivateKey key) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.decodeBase64(msg)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 签名
     * @param param
     * @param secret
     * @return
     * @throws Exception
     */
    public static String makeSign(WechatEncryptionParam param, String secret) throws Exception {

        Gson gson = new Gson();
        String JSONString = gson.toJson(param);
        System.out.println(JSONString);
        Map keyValues = gson.fromJson(JSONString, Map.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(secret);
        List<String> keys = new ArrayList<>(keyValues.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            if (!"_sign".equals(key)) {
                stringBuffer.append(key);
                stringBuffer.append(keyValues.get(key));
            }
        }
        stringBuffer.append(secret);
        String signStr = stringBuffer.toString();
        String md5Hex = DigestUtils.md5Hex(signStr).toUpperCase();
        return md5Hex;
    }
}
