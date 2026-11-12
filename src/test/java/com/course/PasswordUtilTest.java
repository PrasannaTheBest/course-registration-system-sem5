package com.course;

import com.course.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashVerifiesAndIsSalted() {
        String h1 = PasswordUtil.hash("secret12");
        String h2 = PasswordUtil.hash("secret12");
        assertTrue(h1.startsWith("pbkdf2_sha256$" + PasswordUtil.ITERATIONS + "$"));
        assertNotEquals(h1, h2, "same password must give different hashes (random salt)");
        assertTrue(PasswordUtil.verify("secret12", h1));
        assertFalse(PasswordUtil.verify("secret13", h1));
    }

    @Test
    void legacyHashesStillVerify() throws Exception {
        byte[] salt = "0123456789abcdef".getBytes();
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(new PBEKeySpec("oldpass".toCharArray(), salt, 65_536, 256)).getEncoded();
        String legacy = Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        assertTrue(PasswordUtil.verify("oldpass", legacy));
        assertFalse(PasswordUtil.verify("wrong", legacy));
    }

    @Test
    void malformedValuesAreRejectedNotThrown() {
        assertFalse(PasswordUtil.verify("x", null));
        assertFalse(PasswordUtil.verify("x", ""));
        assertFalse(PasswordUtil.verify("x", "garbage"));
        assertFalse(PasswordUtil.verify("x", "pbkdf2_sha256$abc$!!$!!"));
        assertFalse(PasswordUtil.verify("x", "pbkdf2_sha256$600000$onlythree"));
    }
}
