package com.payneteasy.superfly.password;

import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import org.apache.commons.codec.binary.Hex;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * PBKDF2WithHmacSHA256 encoder. Result is deterministic for (password, salt),
 * so it can still be compared in SQL. Stored format:
 * {@code pbkdf2-sha256$<iterations>$<hex of 256-bit key>}.
 * <p>
 * Unlike {@link AbstractPasswordEncoder}, null or empty salt is rejected: there
 * is no meaningful way to fall back to an unsalted password hash.
 */
@Component
@Primary
@OnPolicyCondition({Policy.NONE, Policy.PCIDSS})
public class Pbkdf2PasswordEncoder implements PasswordEncoder {
    public static final String PREFIX     = "pbkdf2-sha256$";
    public static final int    ITERATIONS = 600_000;

    private static final int KEY_BITS       = 256;
    private static final int MAX_ITERATIONS = 10_000_000;

    private final int iterations;

    public Pbkdf2PasswordEncoder() {
        this(ITERATIONS);
    }

    Pbkdf2PasswordEncoder(int iterations) {
        this.iterations = iterations;
    }

    public static boolean isPbkdf2(String storedHash) {
        return storedHash != null && storedHash.startsWith(PREFIX);
    }

    /**
     * Checks a password against a stored hash in this format, using iterations
     * from the stored value.
     *
     * @return false if the stored value is malformed
     */
    public static boolean matches(String plainPassword, String salt, String storedHash) {
        if (!isPbkdf2(storedHash)) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        int storedIterations;
        try {
            storedIterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        if (storedIterations < 1 || storedIterations > MAX_ITERATIONS) {
            return false;
        }
        String candidate = new Pbkdf2PasswordEncoder(storedIterations).encode(plainPassword, salt);
        return MessageDigest.isEqual(
                candidate.getBytes(StandardCharsets.UTF_8), storedHash.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String encode(String plainPassword, String salt) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("Salt must not be empty");
        }
        PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt.getBytes(StandardCharsets.UTF_8),
                iterations, KEY_BITS);
        try {
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return PREFIX + iterations + "$" + new String(Hex.encodeHex(key));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } finally {
            spec.clearPassword();
        }
    }
}
