package com.payneteasy.superfly.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Subsystem main tokens are stored as {@code sha256:<hex>}: a read of the database does not give the credential.
 * The tokens are random (not user-chosen), so a plain fast hash is enough.
 */
public final class SubsystemTokenHasher {

    public static final String PREFIX = "sha256:";

    private SubsystemTokenHasher() {
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

    /**
     * @param presented token from the request
     * @param stored    value of subsystems.subsystem_token
     * @return true if the presented token hashes to the stored value; a stored value without the hash prefix never matches
     */
    public static boolean matches(String presented, String stored) {
        if (presented == null || presented.isEmpty() || stored == null || !stored.startsWith(PREFIX)) {
            return false;
        }
        return MessageDigest.isEqual(hash(presented).getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }
}
