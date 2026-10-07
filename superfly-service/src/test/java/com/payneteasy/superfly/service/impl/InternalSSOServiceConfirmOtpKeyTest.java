package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
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

/**
 * Confirming a pending OTP key is an OTP attempt: a locked account gets no check, every failure counts towards the
 * OTP lockout, otherwise confirmations could be used to guess codes past the limit.
 */
public class InternalSSOServiceConfirmOtpKeyTest {

    private static final String USER = "user";
    private static final String CODE = "123456";

    private UserService userService;
    private HOTPService hotpService;
    private LockoutStrategy lockoutStrategy;
    private InternalSSOServiceImpl service;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        hotpService = EasyMock.createStrictMock(HOTPService.class);
        lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setHotpService(hotpService);
        service.setLockoutStrategy(lockoutStrategy);
        service.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
    }

    private void expectLockLookup(boolean locked) {
        UserForDescription stored = new UserForDescription();
        stored.setUsername(USER);
        UserWithStatus status = new UserWithStatus();
        status.setUserName(USER);
        status.setAccountLocked(locked);
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored);
        EasyMock.expect(userService.getUserStatuses(USER)).andReturn(Collections.singletonList(status));
    }

    private void replayAll() {
        EasyMock.replay(userService, hotpService, lockoutStrategy);
    }

    private void verifyAll() {
        EasyMock.verify(userService, hotpService, lockoutStrategy);
    }

    @Test
    public void lockedAccountIsLockedWithoutCheckingTheCode() {
        expectLockLookup(true);
        // hotpService and lockoutStrategy have no expectations, the failure counter is not touched
        replayAll();

        assertEquals(Status.LOCKED, service.confirmOtpMasterKey(USER, CODE));

        verifyAll();
    }

    @Test
    public void successDoesNotCountAsFailure() {
        expectLockLookup(false);
        EasyMock.expect(hotpService.confirmGoogleAuthMasterKey(USER, CODE)).andReturn(Status.SUCCESS);
        replayAll();

        assertEquals(Status.SUCCESS, service.confirmOtpMasterKey(USER, CODE));

        verifyAll();
    }

    @Test
    public void invalidCodeIsFailedAttempt() {
        assertFailedAttemptKeepsStatus(Status.INVALID);
    }

    @Test
    public void clockSkewIsFailedAttempt() {
        assertFailedAttemptKeepsStatus(Status.CLOCK_SKEW);
    }

    private void assertFailedAttemptKeepsStatus(Status status) {
        expectLockLookup(false);
        EasyMock.expect(hotpService.confirmGoogleAuthMasterKey(USER, CODE)).andReturn(status);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(false);
        replayAll();

        assertEquals(status, service.confirmOtpMasterKey(USER, CODE));

        verifyAll();
    }

    @Test
    public void attemptThatLocksTheAccountIsLocked() {
        expectLockLookup(false);
        EasyMock.expect(hotpService.confirmGoogleAuthMasterKey(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(true);
        replayAll();

        assertEquals(Status.LOCKED, service.confirmOtpMasterKey(USER, CODE));

        verifyAll();
    }

    @Test
    public void unknownUserIsInvalidAndNeverLocked() {
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(null);
        EasyMock.expect(hotpService.confirmGoogleAuthMasterKey(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(null);
        replayAll();

        assertEquals(Status.INVALID, service.confirmOtpMasterKey(USER, CODE));

        verifyAll();
    }
}
