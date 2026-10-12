package com.payneteasy.superfly.api.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * One-time {@code state} value that binds an SSO login to the browser session which started it.
 * <p>
 * A subsystem generates the value before redirecting to the SSO login page, keeps it in its HTTP session
 * under {@link #SESSION_ATTRIBUTE} and passes it as {@link #PARAMETER}. The SSO server returns it together
 * with the subsystem token, and the subsystem accepts the token only if both values match.
 * Servlet-API independent so that javax and jakarta integrations share it.
 */
public final class SSOLoginState {

    public static final String PARAMETER = "state";
    public static final String SESSION_ATTRIBUTE = SSOLoginState.class.getName();

    private static final int STATE_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SSOLoginState() {
    }

    /** 32 random bytes, base64url without padding (43 characters). */
    public static String generate() {
        byte[] bytes = new byte[STATE_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Appends the state parameter to a URL; the generated value needs no URL encoding. */
    public static String appendTo(String url, String state) {
        return url + (url.contains("?") ? "&" : "?") + PARAMETER + "=" + state;
    }

    /** Constant-time comparison; a missing value on either side never matches. */
    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null || expected.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
