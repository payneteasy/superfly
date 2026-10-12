package com.payneteasy.superfly.common.notification;

import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.payneteasy.superfly.common.notification.NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS;
import static com.payneteasy.superfly.common.notification.NotificationSignatures.Result;
import static com.payneteasy.superfly.common.notification.NotificationSignatures.SIGNATURE_PARAMETER;
import static com.payneteasy.superfly.common.notification.NotificationSignatures.TIMESTAMP_PARAMETER;
import static org.junit.Assert.assertEquals;

public class NotificationSignaturesTest {

    private static final long NOW = 1700000000000L;
    private static final String KEY = "sha256:f9a78e5f3c45623757c4d003b1c374381003a5c92f396c3252621af5a5f174f1";

    @Test
    public void testCanonicalize() {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("superflyNotification", new String[]{"logout"});
        params.put("superflyLogoutSessionIds", new String[]{"1,2"});
        params.put("other", new String[]{"ignored"});
        params.put(TIMESTAMP_PARAMETER, new String[]{"5"});
        params.put("superflyMulti", new String[]{"b", "a"});
        params.put(SIGNATURE_PARAMETER, new String[]{"ignored"});

        assertEquals("superflyLogoutSessionIds=1,2\n"
                + "superflyMulti=b,a\n"
                + "superflyNotification=logout\n"
                + "superflyNotificationTimestamp=5",
                NotificationSignatures.canonicalize(params));
    }

    @Test
    public void testSameSignatureAsSuperfly17() {
        // computed by NotificationSignatures of Superfly 1.7 (commit 9953a419) and cross-checked with
        // openssl dgst -sha256 -hmac; the key is the hash of the dummy token "dummy-subsystem-token"
        assertEquals(KEY, SubsystemTokenHashes.hash("dummy-subsystem-token"));
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("superflyNotification", new String[]{"LOGOUT"});
        params.put("superflyLogoutSessionIds", new String[]{"101,102"});
        params.put(TIMESTAMP_PARAMETER, new String[]{"1700000000000"});
        params.put("superflyMulti", new String[]{"b", "a"});
        params.put("unrelated", new String[]{"x"});

        assertEquals("bf3903a4f18ef0696fcc4d75d9cf63777ab3a6cb26c6d5d8b3e1f62ecbc6305e",
                NotificationSignatures.sign(KEY, params));
    }

    @Test
    public void testKnownSignature() {
        // python: hmac.new(b'secret', canonical.encode(), hashlib.sha256).hexdigest()
        assertEquals("dd0f503e367f39ce723b6c455ac401e08b0deeceda2162bdc618316d9bf0635b",
                NotificationSignatures.sign("secret", logoutParams(NOW)));
    }

    @Test
    public void testSignAndVerify() {
        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        assertEquals(Result.VALID, verify(KEY, params, NOW));
        assertEquals(Result.VALID, verify(KEY, params, NOW + DEFAULT_MAX_CLOCK_SKEW_MILLIS));
        assertEquals(Result.VALID, verify(KEY, params, NOW - DEFAULT_MAX_CLOCK_SKEW_MILLIS));
    }

    @Test
    public void testUnrelatedParametersDoNotAffectSignature() {
        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        params.put("jsessionid", new String[]{"x"});
        assertEquals(Result.VALID, verify(KEY, params, NOW));
    }

    @Test
    public void testTamperedParameters() {
        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        params.put("superflyLogoutSessionIds", new String[]{"1,2,3"});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.put("superflyExtra", new String[]{"x"});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.remove("superflyLogoutSessionIds");
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.put(TIMESTAMP_PARAMETER, new String[]{String.valueOf(NOW + 1)});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
    }

    @Test
    public void testWrongKey() {
        assertEquals(Result.BAD_SIGNATURE, verify("other", signedLogoutParams(KEY, NOW), NOW));
    }

    @Test
    public void testRawTokenIsNotTheKey() {
        assertEquals(Result.BAD_SIGNATURE,
                verify(KEY, signedLogoutParams("dummy-subsystem-token", NOW), NOW));
    }

    @Test
    public void testSignatureComparisonCoversWholeValue() {
        // the comparison is MessageDigest.isEqual (constant time): it must reject a difference in any position
        // and a different length, not only a prefix mismatch
        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        String signature = params.get(SIGNATURE_PARAMETER)[0];

        params.put(SIGNATURE_PARAMETER, new String[]{flip(signature, 0)});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
        params.put(SIGNATURE_PARAMETER, new String[]{flip(signature, signature.length() - 1)});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
        params.put(SIGNATURE_PARAMETER, new String[]{signature.substring(0, signature.length() - 1)});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
        params.put(SIGNATURE_PARAMETER, new String[]{signature + "0"});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
        params.put(SIGNATURE_PARAMETER, new String[]{signature.toUpperCase()});
        assertEquals(Result.BAD_SIGNATURE, verify(KEY, params, NOW));
    }

    @Test
    public void testExpiredTimestamp() {
        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        assertEquals(Result.EXPIRED_TIMESTAMP, verify(KEY, params, NOW + DEFAULT_MAX_CLOCK_SKEW_MILLIS + 1));
        assertEquals(Result.EXPIRED_TIMESTAMP, verify(KEY, params, NOW - DEFAULT_MAX_CLOCK_SKEW_MILLIS - 1));
    }

    @Test
    public void testMissingOrBadParameters() {
        assertEquals(Result.MISSING_SIGNATURE, verify(KEY, logoutParams(NOW), NOW));

        Map<String, String[]> params = signedLogoutParams(KEY, NOW);
        params.put(SIGNATURE_PARAMETER, new String[]{""});
        assertEquals(Result.MISSING_SIGNATURE, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.remove(TIMESTAMP_PARAMETER);
        assertEquals(Result.BAD_TIMESTAMP, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.put(TIMESTAMP_PARAMETER, new String[]{"abc"});
        assertEquals(Result.BAD_TIMESTAMP, verify(KEY, params, NOW));

        params = signedLogoutParams(KEY, NOW);
        params.put(SIGNATURE_PARAMETER, new String[]{"a", "b"});
        assertEquals(Result.MISSING_SIGNATURE, verify(KEY, params, NOW));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKeyIsRejected() {
        NotificationSignatures.sign("", logoutParams(NOW));
    }

    private static String flip(String hex, int index) {
        char replacement = hex.charAt(index) == '0' ? '1' : '0';
        return hex.substring(0, index) + replacement + hex.substring(index + 1);
    }

    private static Result verify(String key, Map<String, String[]> params, long now) {
        return NotificationSignatures.verify(key, params, now, DEFAULT_MAX_CLOCK_SKEW_MILLIS);
    }

    private static Map<String, String[]> logoutParams(long timestamp) {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("superflyNotification", new String[]{"logout"});
        params.put("superflyLogoutSessionIds", new String[]{"1,2"});
        params.put(TIMESTAMP_PARAMETER, new String[]{String.valueOf(timestamp)});
        return params;
    }

    private static Map<String, String[]> signedLogoutParams(String key, long timestamp) {
        Map<String, String[]> params = logoutParams(timestamp);
        params.put(SIGNATURE_PARAMETER, new String[]{NotificationSignatures.sign(key, params)});
        return params;
    }
}
