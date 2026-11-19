package group.xuxiake.web.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 注册邮箱的规范化与格式校验。
 */
public final class EmailAddressUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    private EmailAddressUtil() {
    }

    public static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValid(String email) {
        return email != null && email.length() <= 100 && EMAIL_PATTERN.matcher(email).matches();
    }
}
