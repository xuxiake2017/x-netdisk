package group.xuxiake.web;

import group.xuxiake.web.util.PasswordEncoder;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XNetdiskWebApplicationTests {

    @Test
    void shouldVerifyNewBcryptPassword() {
        String encoded = PasswordEncoder.encode("correct-password", "alice");

        assertTrue(encoded.startsWith("$2"));
        assertTrue(PasswordEncoder.isPasswordValid("correct-password", encoded, "alice"));
        assertFalse(PasswordEncoder.isPasswordValid("wrong-password", encoded, "alice"));
        assertFalse(PasswordEncoder.needsUpgrade(encoded));
    }

    @Test
    void shouldKeepLegacyPasswordCompatibleUntilLoginUpgrade() {
        String encoded = new SimpleHash("MD5", "legacy-password", "alice", 1024).toString();

        assertTrue(PasswordEncoder.isPasswordValid("legacy-password", encoded, "alice"));
        assertFalse(PasswordEncoder.isPasswordValid("wrong-password", encoded, "alice"));
        assertTrue(PasswordEncoder.needsUpgrade(encoded));
    }

}
