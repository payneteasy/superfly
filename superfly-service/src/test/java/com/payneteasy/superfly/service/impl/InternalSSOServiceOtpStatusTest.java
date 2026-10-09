package com.payneteasy.superfly.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * A locked account gets no OTP checks; every failure reason counts as a failed attempt and may lock the account.
 */
public class InternalSSOServiceOtpStatusTest {

    private static final String USER = "user";
    private static final String CODE = "123456";

    private UserService userService;
    private HOTPService hotpService;
    private LockoutStrategy lockoutStrategy;
    private InternalSSOServiceImpl service;
    private final Logger serviceLogger = (Logger) LoggerFactory.getLogger(InternalSSOServiceImpl.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @Before
    public void setUp() {
        userService = EasyMock.createMock(UserService.class);
        hotpService = EasyMock.createStrictMock(HOTPService.class);
        lockoutStrategy = EasyMock.createStrictMock(LockoutStrategy.class);
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setHotpService(hotpService);
        service.setLockoutStrategy(lockoutStrategy);
        service.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        appender.start();
        serviceLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        serviceLogger.detachAppender(appender);
    }

    private static List<UserWithStatus> statuses(String username, boolean locked) {
        UserWithStatus user = new UserWithStatus();
        user.setUserName(username);
        user.setAccountLocked(locked);
        return Collections.singletonList(user);
    }

    private static UserForDescription stored(String username) {
        return stored(username, OTPType.NONE, false);
    }

    private static UserForDescription stored(String username, OTPType type, boolean optional) {
        UserForDescription user = new UserForDescription();
        user.setUsername(username);
        user.setOtpTypeCode(type.code());
        user.setOtpOptional(optional);
        return user;
    }

    /** The lock lookup resolves the stored name first, like the key lookup does. */
    private void expectLockLookup(String requested, String storedName, boolean locked) {
        EasyMock.expect(userService.getUserForDescription(requested)).andReturn(stored(storedName));
        EasyMock.expect(userService.getUserStatuses(storedName)).andReturn(statuses(storedName, locked));
    }

    private void replayAll() {
        EasyMock.replay(userService, hotpService, lockoutStrategy);
    }

    private void verifyAll() {
        EasyMock.verify(userService, hotpService, lockoutStrategy);
    }

    private void expectFailedAttempt(boolean lockedAfterwards) {
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(USER, USER, lockedAfterwards);
    }

    @Test
    public void lockedAccountIsLockedWithoutCheckingTheCode() {
        expectLockLookup(USER, USER, true);
        // checkOtp looks the stored settings up, then the lock
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored(USER, OTPType.GOOGLE_AUTH, false)).times(2);
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn("encrypted-key");
        EasyMock.expect(userService.getUserStatuses(USER)).andReturn(statuses(USER, true));
        // hotpService and lockoutStrategy have no expectations, the failure counter is not touched
        replayAll();

        assertEquals(Status.LOCKED, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));
        assertEquals(Status.LOCKED, service.checkOtp(OTPType.GOOGLE_AUTH, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void successClearsFailures() {
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.SUCCESS);
        userService.clearHOTPLoginsFailed(USER);
        replayAll();

        assertEquals(Status.SUCCESS, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void alreadyUsedIsFailedAttempt() {
        assertFailedAttemptKeepsStatus(Status.ALREADY_USED);
    }

    @Test
    public void clockSkewIsFailedAttempt() {
        assertFailedAttemptKeepsStatus(Status.CLOCK_SKEW);
    }

    @Test
    public void invalidIsFailedAttempt() {
        assertFailedAttemptKeepsStatus(Status.INVALID);
    }

    private void assertFailedAttemptKeepsStatus(Status status) {
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(status);
        expectFailedAttempt(false);
        replayAll();

        assertEquals(status, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void attemptThatLocksTheAccountIsLocked() {
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.CLOCK_SKEW);
        expectFailedAttempt(true);
        replayAll();

        assertEquals(Status.LOCKED, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void unknownUserIsNeverLocked() {
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(null).times(2);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        replayAll();

        assertEquals(Status.INVALID, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        verifyAll();
    }

    @Test
    public void lockOfAnotherUserMatchedByTheProcedureIsIgnored() {
        // get_user_statuses splits its argument on commas, so "a,user" also returns the row of "a"
        String username = "a," + USER;
        EasyMock.expect(userService.getUserForDescription(username)).andReturn(stored(username));
        EasyMock.expect(userService.getUserStatuses(username)).andReturn(statuses("a", true));
        EasyMock.expect(hotpService.validateGoogleTimePassword(username, CODE)).andReturn(Status.SUCCESS);
        userService.clearHOTPLoginsFailed(username);
        replayAll();

        assertEquals(Status.SUCCESS, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, username, CODE));

        verifyAll();
    }

    @Test
    public void lockedAccountRequestedWithTrailingSpaceIsLocked() {
        // "user " finds the key of "user" (PAD SPACE), so it must find the lock of "user" too
        String requested = USER + " ";
        EasyMock.expect(userService.getUserForDescription(requested)).andReturn(stored(USER));
        EasyMock.expect(userService.getUserStatuses(USER)).andReturn(statuses(USER, true));
        EasyMock.expect(userService.getUserStatuses(requested)).andReturn(Collections.emptyList()).anyTimes();
        // hotpService and lockoutStrategy have no expectations
        replayAll();

        assertEquals(Status.LOCKED, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, requested, CODE));

        verifyAll();
    }

    @Test
    public void optionalOtpWithoutKeySucceedsWithoutChecks() {
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored(USER, OTPType.GOOGLE_AUTH, true));
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(null);
        replayAll();

        assertEquals(Status.SUCCESS, service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, ""));

        verifyAll();
    }
    @Test
    public void failureLogCarriesTheStatusAfterTheLockout() {
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        expectFailedAttempt(true);
        replayAll();

        assertEquals(Status.LOCKED, service.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, CODE));

        assertEquals(Collections.singletonList("OTP check failed user: LOCKED"),
                appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(java.util.stream.Collectors.toList()));
    }

    private void expectStoredAndKey(OTPType type, boolean optional, String key) {
        EasyMock.expect(userService.getUserForDescription(USER)).andReturn(stored(USER, type, optional));
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(key);
    }

    @Test
    public void typeNoneFromCallerIsIgnoredWhenStoredTypeIsGoogleAuth() {
        expectStoredAndKey(OTPType.GOOGLE_AUTH, false, "encrypted-key");
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(USER, USER, false);
        // the lock lookup before the check
        expectLockLookup(USER, USER, false);
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.NONE, true, USER, CODE));

        verifyAll();
    }

    @Test
    public void storedNoneWithoutKeySucceedsAndKeepsTheFailureCounter() {
        expectStoredAndKey(OTPType.NONE, false, null);
        expectLockLookup(USER, USER, false);
        // no clearHOTPLoginsFailed, no code check
        replayAll();

        assertEquals(Status.SUCCESS, service.checkOtp(OTPType.NONE, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void storedNoneWithoutKeyOfLockedAccountIsLocked() {
        expectStoredAndKey(OTPType.NONE, false, null);
        expectLockLookup(USER, USER, true);
        replayAll();

        assertEquals(Status.LOCKED, service.checkOtp(OTPType.NONE, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void storedNoneWithoutKeyIgnoresGoogleAuthFromCaller() {
        expectStoredAndKey(OTPType.NONE, false, null);
        expectLockLookup(USER, USER, false);
        replayAll();

        assertEquals(Status.SUCCESS, service.checkOtp(OTPType.GOOGLE_AUTH, false, USER, CODE));

        verifyAll();
    }

    @Test
    public void storedNoneWithKeyStillChecksTheCode() {
        expectStoredAndKey(OTPType.NONE, false, "encrypted-key");
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, CODE)).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(USER, USER, false);
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.NONE, true, USER, CODE));

        verifyAll();
    }

    @Test
    public void optionalFlagFromCallerIsIgnored() {
        expectStoredAndKey(OTPType.GOOGLE_AUTH, false, null);
        expectLockLookup(USER, USER, false);
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, "")).andReturn(Status.INVALID);
        userService.incrementHOTPLoginsFailed(USER);
        lockoutStrategy.checkLoginsFailed(USER, LockoutType.HOTP);
        expectLockLookup(USER, USER, false);
        replayAll();

        assertEquals(Status.INVALID, service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, ""));

        verifyAll();
    }

    @Test
    public void mismatchWithTheStoredSettingsIsLoggedWithoutTheCode() {
        serviceLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        expectStoredAndKey(OTPType.NONE, false, null);
        expectLockLookup(USER, USER, false);
        replayAll();

        service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, CODE);

        boolean logged = appender.list.stream().anyMatch(event ->
                event.getFormattedMessage().contains("differ from the stored ones")
                        && !event.getFormattedMessage().contains(CODE));
        org.junit.Assert.assertTrue(logged);
    }
}
