package com.course.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted PBKDF2-HMAC-SHA256 password hashing.
 *
 * <p>Stored format: {@code pbkdf2_sha256$<iterations>$<base64 salt>$<base64 hash>}. The iteration count is
 * stored with every hash so it can be raised later without breaking existing accounts. 600,000 iterations
 * is the figure the OWASP Password Storage Cheat Sheet recommends for PBKDF2-HMAC-SHA256.
 * Hashes written by the first version of this project ({@code salt:hash}, 65,536 iterations) still verify.
 */
public final class PasswordUtil {
    public static final int ITERATIONS = 600_000;
    private static final String PREFIX = "pbkdf2_sha256";
    private static final int LEGACY_ITERATIONS = 65_536;
    private static final int KEY_BITS = 256;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (stored == null) {
            return false;
        }
        try {
            int iterations;
            String saltB64;
            String hashB64;
            if (stored.startsWith(PREFIX + "$")) {
                String[] parts = stored.split("\\$");
                if (parts.length != 4) {
                    return false;
                }
                iterations = Integer.parseInt(parts[1]);
                saltB64 = parts[2];
                hashB64 = parts[3];
            } else if (stored.contains(":")) { // legacy format
                String[] parts = stored.split(":", 2);
                iterations = LEGACY_ITERATIONS;
                saltB64 = parts[0];
                hashB64 = parts[1];
            } else {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(saltB64);
            byte[] expected = Base64.getDecoder().decode(hashB64);
            return MessageDigest.isEqual(expected, pbkdf2(password.toCharArray(), salt, iterations)); // constant time
        } catch (IllegalArgumentException e) {
            return false; // malformed stored value
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is not available", e);
        }
    }
}
