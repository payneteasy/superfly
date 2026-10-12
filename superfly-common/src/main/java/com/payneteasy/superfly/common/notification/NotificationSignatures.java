package com.payneteasy.superfly.common.notification;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * Signs and verifies notifications sent by a Superfly server to subsystem
 * callback URLs.
 * <p>
 * Signature is a lowercase hex HMAC-SHA256 of the canonical string: all request
 * parameters whose names start with {@value #PARAMETER_PREFIX} (except the
 * signature itself), sorted by name, each as {@code name=value} (values of a
 * multi-valued parameter are joined with ',' in the order they were sent),
 * joined with '\n'. The timestamp parameter (epoch millis) is a part of the
 * canonical string.
 * <p>
 * The format is the same as in Superfly 1.7; the key differs: here it is the
 * stored hash of the subsystem token ({@link com.payneteasy.superfly.common.utils.SubsystemTokenHashes#hash(String)}),
 * which the server has and the subsystem computes from its token.
 */
public final class NotificationSignatures {

    public static final String PARAMETER_PREFIX = "superfly";
    public static final String TIMESTAMP_PARAMETER = "superflyNotificationTimestamp";
    public static final String SIGNATURE_PARAMETER = "superflyNotificationSignature";

    /** Maximum allowed difference between notification timestamp and local clock. */
    public static final long DEFAULT_MAX_CLOCK_SKEW_MILLIS = 5 * 60 * 1000L;

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    public enum Result {
        VALID,
        MISSING_SIGNATURE,
        BAD_TIMESTAMP,
        EXPIRED_TIMESTAMP,
        BAD_SIGNATURE
    }

    private NotificationSignatures() {
    }

    /**
     * Builds a canonical string to be signed.
     *
     * @param parameters request parameters (name to values in sent order)
     * @return canonical string
     */
    public static String canonicalize(Map<String, String[]> parameters) {
        Map<String, String[]> sorted = new TreeMap<>();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String name = entry.getKey();
            if (name != null && name.startsWith(PARAMETER_PREFIX) && !SIGNATURE_PARAMETER.equals(name)) {
                sorted.put(name, entry.getValue());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(entry.getKey()).append('=');
            String[] values = entry.getValue();
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    if (values[i] != null) {
                        sb.append(values[i]);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Computes a signature for the given parameters (the signature parameter,
     * if present, is ignored).
     *
     * @param key        stored hash of the subsystem token
     * @param parameters request parameters (name to values in sent order)
     * @return lowercase hex signature
     */
    public static String sign(String key, Map<String, String[]> parameters) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be empty");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(canonicalize(parameters).getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot compute " + HMAC_ALGORITHM, e);
        }
    }

    /**
     * Verifies signature and timestamp of the given parameters.
     *
     * @param key            stored hash of the subsystem token
     * @param parameters     request parameters (name to values in sent order)
     * @param nowMillis      current time
     * @param maxSkewMillis  maximum allowed difference between timestamp and now
     * @return verification result
     */
    public static Result verify(String key, Map<String, String[]> parameters,
            long nowMillis, long maxSkewMillis) {
        String signature = getSingleValue(parameters, SIGNATURE_PARAMETER);
        if (signature == null || signature.isEmpty()) {
            return Result.MISSING_SIGNATURE;
        }
        String timestampString = getSingleValue(parameters, TIMESTAMP_PARAMETER);
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException e) {
            return Result.BAD_TIMESTAMP;
        }
        if (Math.abs(nowMillis - timestamp) > maxSkewMillis) {
            return Result.EXPIRED_TIMESTAMP;
        }
        String expected = sign(key, parameters);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            return Result.BAD_SIGNATURE;
        }
        return Result.VALID;
    }

    private static String getSingleValue(Map<String, String[]> parameters, String name) {
        String[] values = parameters.get(name);
        if (values == null || values.length != 1) {
            return null;
        }
        return values[0];
    }

    private static String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = HEX[(bytes[i] >> 4) & 0xf];
            chars[i * 2 + 1] = HEX[bytes[i] & 0xf];
        }
        return new String(chars);
    }
}
