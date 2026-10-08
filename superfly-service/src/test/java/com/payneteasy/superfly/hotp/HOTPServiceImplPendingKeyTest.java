package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.service.impl.InternalSSOServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

/**
 * A reset key is pending until a code from it is confirmed: the active key keeps working, the pending one is never
 * accepted at login. Runs against an in-memory users row that behaves like the stored procedures.
 */
public class HOTPServiceImplPendingKeyTest {

    private static final String USER = "user";
    private static final long STEP_MILLIS = 30_000L;
    private static final long NOW = 1_000_000 * STEP_MILLIS + 5_000;
    private static final long STEP = NOW / STEP_MILLIS;

    private HOTPServiceImpl service;
    private CryptoServiceImpl crypto;
    private UsersRow row;
    private UserService userService;
    private AtomicLong clock;
    private String activeSecret;

    /** The columns of one users row the OTP procedures work with. */
    private static class UsersRow {
        String masterKey;
        String pendingMasterKey;
        Long lastUsedStep;
        // the confirmation must store its step in the CAS update itself, not in a separate call
        int separateStepMarks;
        // runs right after the pending key has been read: lets a test slip in a concurrent reset
        Runnable afterPendingRead = () -> { };
    }

    @Before
    public void setUp() throws Exception {
        crypto = new CryptoServiceImpl("test-secret", "test-salt");
        row = new UsersRow();
        userService = fakeUserService(row);
        service = new HOTPServiceImpl();
        service.setCryptoService(crypto);
        service.setUserService(userService);
        clock = new AtomicLong(NOW);
        service.setClock(clock::get);
        activeSecret = service.getGoogleAuthenticator().get().createCredentials().getKey();
        row.masterKey = crypto.encrypt(activeSecret);
    }

    private static UserService fakeUserService(UsersRow row) {
        return (UserService) Proxy.newProxyInstance(UserService.class.getClassLoader(), new Class<?>[]{UserService.class},
                (proxy, method, args) -> {
                    assertEquals(USER, args[0]);
                    switch (method.getName()) {
                        case "getOtpMasterKeyByUsername":
                            return row.masterKey;
                        case "getOtpPendingMasterKeyByUsername":
                            String pending = row.pendingMasterKey;
                            row.afterPendingRead.run();
                            return pending;
                        case "persistOtpPendingMasterKey":
                            row.pendingMasterKey = (String) args[1];
                            return null;
                        case "persistOtpMasterKeyForUsername":
                            // save_google_auth_master_key: admin reset (null) and a newly set up key
                            row.masterKey = (String) args[1];
                            row.pendingMasterKey = null;
                            return null;
                        case "updateUserOtpType":
                            return null;
                        case "confirmOtpPendingMasterKey":
                            if (row.pendingMasterKey == null || !row.pendingMasterKey.equals(args[1])) {
                                return false;
                            }
                            row.masterKey = row.pendingMasterKey;
                            row.pendingMasterKey = null;
                            long confirmedStep = (Long) args[2];
                            row.lastUsedStep = row.lastUsedStep == null ? confirmedStep : Math.max(row.lastUsedStep, confirmedStep);
                            return true;
                        case "markOtpStepUsed":
                            row.separateStepMarks++;
                            long step = (Long) args[1];
                            if (row.lastUsedStep != null && row.lastUsedStep >= step) {
                                return false;
                            }
                            row.lastUsedStep = step;
                            return true;
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private String codeOf(String secret, long step) {
        return String.format("%06d", service.getGoogleAuthenticator().get().getTotpPassword(secret, step * STEP_MILLIS));
    }

    /** Skips the test in the unlikely case the code of the step equals a code of another step used by the tests. */
    private void assumeUniqueInSkewWindow(String secret, long step) {
        for (long other = STEP - 3; other <= STEP + 4; other++) {
            if (other != step) {
                assumeTrue(!codeOf(secret, other).equals(codeOf(secret, step)));
            }
        }
    }

    /** Skips the test in the unlikely case the two keys produce the same code around now. */
    private void assumeDifferentCodes(String secret, String otherSecret) {
        for (long step = STEP - 3; step <= STEP + 4; step++) {
            for (long other = STEP - 3; other <= STEP + 4; other++) {
                assumeTrue(!codeOf(secret, step).equals(codeOf(otherSecret, other)));
            }
        }
    }

    private String decrypt(String encrypted) throws Exception {
        return crypto.decrypt(encrypted);
    }

    @Test
    public void resetStoresThePendingKeyAndLeavesTheActiveKeyAlone() throws Exception {
        String activeBefore = row.masterKey;

        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);

        assertEquals(activeBefore, row.masterKey);
        assertEquals(newSecret, decrypt(row.pendingMasterKey));
        assertNotEquals(activeSecret, newSecret);
    }

    @Test
    public void activeKeyWorksAndPendingKeyIsRejectedUntilConfirmation() throws Exception {
        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        assumeDifferentCodes(activeSecret, newSecret);
        assumeUniqueInSkewWindow(newSecret, STEP);

        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, codeOf(newSecret, STEP)));
        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOf(activeSecret, STEP)));
        assertEquals(newSecret, decrypt(row.pendingMasterKey));
    }

    @Test
    public void confirmationMakesThePendingKeyActive() throws Exception {
        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String pendingCiphertext = row.pendingMasterKey;
        assumeDifferentCodes(activeSecret, newSecret);
        assumeUniqueInSkewWindow(newSecret, STEP);
        assumeUniqueInSkewWindow(newSecret, STEP + 1);

        assertEquals(Status.SUCCESS, service.confirmGoogleAuthMasterKey(USER, codeOf(newSecret, STEP)));

        assertEquals(pendingCiphertext, row.masterKey);
        assertNull(row.pendingMasterKey);
        assertEquals(Long.valueOf(STEP), row.lastUsedStep);
        assertEquals(0, row.separateStepMarks);
        // the confirmation code is used up, the old key is gone, the next code of the new key logs in
        assertEquals(Status.ALREADY_USED, service.validateGoogleTimePassword(USER, codeOf(newSecret, STEP)));
        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, codeOf(activeSecret, STEP + 1)));
        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOf(newSecret, STEP + 1)));
    }

    @Test
    public void repeatedResetReplacesThePendingKey() throws Exception {
        String firstSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String secondSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        assumeDifferentCodes(firstSecret, secondSecret);
        assumeUniqueInSkewWindow(secondSecret, STEP);

        assertEquals(secondSecret, decrypt(row.pendingMasterKey));
        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(firstSecret, STEP)));
        assertEquals(Status.SUCCESS, service.confirmGoogleAuthMasterKey(USER, codeOf(secondSecret, STEP)));
        assertEquals(secondSecret, decrypt(row.masterKey));
    }

    @Test
    public void confirmationWithoutPendingKeyIsInvalid() throws Exception {
        String activeBefore = row.masterKey;

        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(activeSecret, STEP)));

        assertEquals(activeBefore, row.masterKey);
        assertNull(row.lastUsedStep);
    }

    @Test
    public void wrongOrMalformedConfirmationCodeKeepsThePendingKey() throws Exception {
        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String activeBefore = row.masterKey;
        String pendingBefore = row.pendingMasterKey;
        assumeDifferentCodes(activeSecret, newSecret);

        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(activeSecret, STEP)));
        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, "12345"));
        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, null));

        assertEquals(activeBefore, row.masterKey);
        assertEquals(pendingBefore, row.pendingMasterKey);
        assertNull(row.lastUsedStep);
    }

    @Test
    public void skewedConfirmationCodeIsClockSkewAndKeepsThePendingKey() throws Exception {
        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String pendingBefore = row.pendingMasterKey;
        assumeUniqueInSkewWindow(newSecret, STEP + 2);

        assertEquals(Status.CLOCK_SKEW, service.confirmGoogleAuthMasterKey(USER, codeOf(newSecret, STEP + 2)));

        assertEquals(pendingBefore, row.pendingMasterKey);
        assertNull(row.lastUsedStep);
    }

    @Test
    public void confirmationLosesToAConcurrentReset() throws Exception {
        String newSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String activeBefore = row.masterKey;
        assumeUniqueInSkewWindow(newSecret, STEP);
        String concurrentPending = crypto.encrypt(service.getGoogleAuthenticator().get().createCredentials().getKey());
        row.afterPendingRead = () -> row.pendingMasterKey = concurrentPending;

        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(newSecret, STEP)));

        assertEquals(activeBefore, row.masterKey);
        assertEquals(concurrentPending, row.pendingMasterKey);
        assertNull(row.lastUsedStep);
    }

    @Test
    public void pendingKeyAloneIsNoMasterKey() throws Exception {
        row.masterKey = null;
        service.resetGoogleAuthMasterKey("subsystem", USER);
        InternalSSOServiceImpl internal = new InternalSSOServiceImpl();
        internal.setUserService(userService);

        assertFalse(internal.hasOtpMasterKey(USER));
        assertNotNull(row.pendingMasterKey);
    }

    @Test
    public void adminResetOutdatesThePendingKey() throws Exception {
        String pendingSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        assumeUniqueInSkewWindow(pendingSecret, STEP);

        // the "reset OTP" link of the user details page
        userService.persistOtpMasterKeyForUsername(USER, null);

        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(pendingSecret, STEP)));
        assertNull(row.masterKey);
        assertNull(row.lastUsedStep);
    }

    @Test
    public void keySetUpOnTheSsoPageOutdatesThePendingKey() throws Exception {
        String pendingSecret = service.resetGoogleAuthMasterKey("subsystem", USER);
        String setUpSecret = service.getGoogleAuthenticator().get().createCredentials().getKey();
        assumeDifferentCodes(pendingSecret, setUpSecret);
        assumeUniqueInSkewWindow(pendingSecret, STEP);

        // SSOSetupGoogleAuthPage and the init-OTP filter store the key they have checked this way
        service.persistOtpKey(OTPType.GOOGLE_AUTH, USER, setUpSecret);

        assertEquals(Status.INVALID, service.confirmGoogleAuthMasterKey(USER, codeOf(pendingSecret, STEP)));
        assertEquals(setUpSecret, decrypt(row.masterKey));
        assertNull(row.pendingMasterKey);
    }
}
