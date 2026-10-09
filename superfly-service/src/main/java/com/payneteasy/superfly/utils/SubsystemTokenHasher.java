package com.payneteasy.superfly.utils;

import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Subsystem main tokens are stored as {@code sha256:<hex>}: a read of the database does not give the credential.
 * The tokens are random (not user-chosen), so a plain fast hash is enough. The hash itself is
 * {@link SubsystemTokenHashes}: clients compute the same value to verify notification signatures.
 */
public final class SubsystemTokenHasher {

    public static final String PREFIX = SubsystemTokenHashes.PREFIX;

    private SubsystemTokenHasher() {
    }

    public static String hash(String token) {
        return SubsystemTokenHashes.hash(token);
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
