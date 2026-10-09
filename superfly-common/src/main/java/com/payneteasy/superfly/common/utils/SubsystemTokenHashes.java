package com.payneteasy.superfly.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash of a subsystem main token as the server stores it: {@code sha256:<hex>}.
 * <p>
 * Shared by the server and the clients: the server keeps only this value, and both sides use it as the key
 * of notification signatures (see {@link com.payneteasy.superfly.common.notification.NotificationSignatures}).
 */
public final class SubsystemTokenHashes {

    public static final String PREFIX = "sha256:";

    private SubsystemTokenHashes() {
    }

    public static String hash(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Subsystem token must not be empty");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
