package com.payneteasy.superfly.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates unguessable bearer tokens (SSO session ids, subsystem tokens).
 */
public final class SecureTokens {

    private static final int          TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM      = new SecureRandom();

    private SecureTokens() {
    }

    /**
     * @return prefix followed by 32 random bytes in unpadded base64url (43 chars)
     */
    public static String generate(String prefix) {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return prefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
