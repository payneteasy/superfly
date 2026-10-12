package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The OTP step of the admin console login refuses a locked or suspended account: the code is not checked and the
 * failed OTP counter stays as it is.
 */
public class UserServiceOtpAccountStateTest {

    private static final String USER    = "user";
    private static final long   USER_ID = 7L;
    private static final String CODE    = "123456";

    private UserDao         userDao;
    private HOTPService     hotpService;
    private LockoutStrategy lockoutStrategy;
    private LoggerSink      loggerSink;
    private UserServiceImpl service;

    @Before
    public void setUp() {
        userDao = createStrictMock(UserDao.class);
        hotpService = createStrictMock(HOTPService.class);
        lockoutStrategy = createStrictMock(LockoutStrategy.class);
        loggerSink = createStrictMock(LoggerSink.class);
        service = new UserServiceImpl();
        service.setUserDao(userDao);
        service.setHotpService(hotpService);
        service.setLockoutStrategy(lockoutStrategy);
        service.setLoggerSink(loggerSink);
    }

    private void expectStoredUser(OTPType type) {
        UserForDescription stored = new UserForDescription();
        stored.setUserId(USER_ID);
        stored.setUsername(USER);
        stored.setOtpTypeCode(type.code());
        expect(userDao.getUserForDescription(USER)).andReturn(stored);
    }

    private static UIUserDetails accountState(boolean locked, boolean suspended) {
        UIUserDetails state = new UIUserDetails();
        state.setId(USER_ID);
        state.setAccountLocked(locked);
        state.setAccountSuspended(suspended);
        return state;
    }

    private void expectRefusal() {
        // the security log carries the reason; the caller only gets "false", like for a wrong code
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_OTP_CHECK"), eq(false), eq(USER), eq("status=LOCKED"));
    }

    private void replayAll() {
        replay(userDao, hotpService, lockoutStrategy, loggerSink);
    }

    private void verifyAll() {
        verify(userDao, hotpService, lockoutStrategy, loggerSink);
    }

    @Test
    public void lockedAccountIsRefusedWithoutCheckingTheCode() {
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(true, false));
        expectRefusal();
        // hotpService, lockoutStrategy and the failure counter of the dao have no expectations
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void suspendedAccountIsRefusedWithoutCheckingTheCode() {
        // the suspension procedure sets both flags
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(true, true));
        expectRefusal();
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void suspendedAccountWithoutTheLockFlagIsRefused() {
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(false, true));
        expectRefusal();
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void lockedAccountWithoutOtpIsRefused() {
        // an account without OTP passes the check of the code, but not the check of its state
        expectStoredUser(OTPType.NONE);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(true, false));
        expectRefusal();
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, null));

        verifyAll();
    }

    @Test
    public void accountThatIsGoneIsRefusedWithoutCheckingTheCode() {
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(null);
        expectRefusal();
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void unknownUserIsRefusedWithoutLookingUpTheState() {
        expect(userDao.getUserForDescription(USER)).andReturn(null);
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void activeAccountWithValidCodeSucceeds() throws Exception {
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(false, false)).anyTimes();
        expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.SUCCESS);
        userDao.clearHOTPLoginsFailed(USER);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_OTP_CHECK"), eq(true), eq(USER));
        replayAll();

        assertTrue(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }

    @Test
    public void activeAccountWithoutOtpSucceeds() {
        expectStoredUser(OTPType.NONE);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(false, false)).anyTimes();
        userDao.clearHOTPLoginsFailed(USER);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_OTP_CHECK"), eq(true), eq(USER));
        replayAll();

        assertTrue(service.authenticateUsingOTP(USER, null));

        verifyAll();
    }

    @Test
    public void activeAccountWithWrongCodeStillCountsTheFailure() throws Exception {
        expectStoredUser(OTPType.GOOGLE_AUTH);
        expect(userDao.getUser(USER_ID)).andReturn(accountState(false, false)).anyTimes();
        expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        userDao.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        loggerSink.info(anyObject(Logger.class), eq("REMOTE_OTP_CHECK"), eq(false), eq(USER));
        replayAll();

        assertFalse(service.authenticateUsingOTP(USER, CODE));

        verifyAll();
    }
}
