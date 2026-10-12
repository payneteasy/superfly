package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.hotp.HOTPServiceImpl;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * A code checked for a user without a key is refused with INVALID: there is nothing to guess, so the attempt is
 * not counted and cannot lock the account. A key that exists but cannot be decrypted is still a failure.
 */
public class InternalSSOServiceOtpWithoutKeyTest {

    private static final String USER = "user";
    private static final String CODE = "123456";

    private UserService userService;
    private HOTPService hotpService;
    private LockoutStrategy lockoutStrategy;
    private LoggerSink loggerSink;
    private InternalSSOServiceImpl service;

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        hotpService = EasyMock.createMock(HOTPService.class);
        // no expectations unless a test sets them: any other call fails the test
        lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        loggerSink = EasyMock.createStrictMock(LoggerSink.class);
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setHotpService(hotpService);
        service.setLockoutStrategy(lockoutStrategy);
        service.setLoggerSink(loggerSink);
    }

    private void expectAccount(boolean otpOptional, String encryptedKey) {
        UserForDescription stored = new UserForDescription();
        stored.setUsername(USER);
        stored.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        stored.setOtpOptional(otpOptional);
        UserWithStatus status = new UserWithStatus();
        status.setUserName(USER);
        status.setAccountLocked(false);
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored).anyTimes();
        EasyMock.expect(userService.getUserStatuses(USER)).andReturn(Collections.singletonList(status)).anyTimes();
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(encryptedKey).anyTimes();
    }

    private void expectSecurityLogRecord(boolean success, String details) {
        loggerSink.info(EasyMock.anyObject(org.slf4j.Logger.class), EasyMock.eq("REMOTE_OTP_CHECK"),
                EasyMock.eq(success), EasyMock.eq(USER), EasyMock.eq(details));
    }

    private void replayAll() {
        EasyMock.replay(userService, hotpService, lockoutStrategy, loggerSink);
    }

    private void verifyAll() {
        EasyMock.verify(userService, hotpService, lockoutStrategy, loggerSink);
    }

    @Test
    public void checkOtpOfUserWithoutKeyIsInvalidAndNotCounted() {
        expectAccount(false, null);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE))
                .andThrow(new SsoDecryptException("GA master key for " + USER + " is null"));
        // neither the failure counter nor the lockout strategy is touched: they have no expectations
        expectSecurityLogRecord(false, "status=INVALID, reason=no key");
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.GOOGLE_AUTH, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void checkOtpOfOptionalUserWithoutKeyAndWithACodeIsInvalid() {
        expectAccount(true, null);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE))
                .andThrow(new SsoDecryptException("GA master key for " + USER + " is null"));
        expectSecurityLogRecord(false, "status=INVALID, reason=no key");
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, CODE));

        verifyAll();
    }

    @Test
    public void authenticateByOtpTypeOfUserWithoutKeyIsInvalidAndNotCounted() {
        expectAccount(false, null);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE))
                .andThrow(new SsoDecryptException("GA master key for " + USER + " is null"));
        expectSecurityLogRecord(false, "status=INVALID, reason=no key");
        replayAll();

        assertEquals(Status.INVALID, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void keyThatCannotBeDecryptedStaysAnExceptionAndIsNotCounted() {
        expectAccount(false, "encrypted-key");
        SsoDecryptException failure = new SsoDecryptException("decrypt error", new RuntimeException("cause"));
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andThrow(failure);
        // as before: nothing is written to the security log, nothing is counted
        replayAll();

        try {
            service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE);
            fail("a key that cannot be decrypted must not be reported as a wrong code");
        } catch (SsoDecryptException e) {
            assertSame(failure, e);
        }

        verifyAll();
    }

    @Test
    public void wrongCodeOfUserWithKeyIsStillCounted() {
        expectAccount(false, "encrypted-key");
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectSecurityLogRecord(false, "status=INVALID");
        replayAll();

        assertEquals(Status.INVALID, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void validCodeOfUserWithKeyStillClearsTheFailures() {
        expectAccount(false, "encrypted-key");
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.SUCCESS);
        userService.clearHOTPLoginsFailed(USER);
        expectSecurityLogRecord(true, "status=SUCCESS");
        replayAll();

        assertEquals(Status.SUCCESS, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    /** The two cases through the real HOTP service: the presence of the key decides, not the text of the exception. */
    private void useRealHotpService() {
        HOTPServiceImpl realHotp = new HOTPServiceImpl();
        realHotp.setUserService(userService);
        realHotp.setCryptoService(new CryptoServiceImpl("test-secret", "test-salt"));
        service.setHotpService(realHotp);
    }

    @Test
    public void realHotpServiceWithoutKeyGivesInvalidWithoutChangingTheState() {
        useRealHotpService();
        // the user mock has no expectation for marking a step as used or for the counter: such a call fails
        expectAccount(false, null);
        expectSecurityLogRecord(false, "status=INVALID, reason=no key");
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.GOOGLE_AUTH, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void realHotpServiceWithBrokenCiphertextFails() {
        useRealHotpService();
        expectAccount(false, "not-a-ciphertext");
        replayAll();

        try {
            service.checkOtp(OTPType.GOOGLE_AUTH, false, USER, CODE);
            fail("a broken stored key must not be reported as a wrong code");
        } catch (SsoDecryptException e) {
            assertEquals("decrypt error", e.getMessage());
        }

        verifyAll();
    }
}
