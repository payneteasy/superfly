package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.hotp.HOTPServiceImpl;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.service.impl.InternalSSOServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * A code of a user that is unknown to the calling subsystem gets the same answer as a code of its own user who has
 * no key: INVALID, no exception. Nothing of the other user is read or changed, and only the own user's check is
 * written to the security log with the reason.
 */
public class SSOServiceImplCheckOtpParityTest {

    private static final String CALLER = "caller";
    private static final String OWN = "own";
    private static final String FOREIGN = "foreign";
    private static final String CODE = "123456";

    private UserService userService;
    private LoggerSink loggerSink;
    private SSOServiceImpl ssoService;

    @Before
    public void setUp() {
        // no expectations unless a test sets them: any other call (a read of the other user, a counter, a step) fails
        userService = createMock(UserService.class);
        loggerSink = createStrictMock(LoggerSink.class);

        HOTPServiceImpl hotpService = new HOTPServiceImpl();
        hotpService.setUserService(userService);
        hotpService.setCryptoService(new CryptoServiceImpl("test-secret", "test-salt"));

        InternalSSOServiceImpl internal = new InternalSSOServiceImpl();
        internal.setUserService(userService);
        internal.setHotpService(hotpService);
        internal.setLockoutStrategy(createStrictMock(LockoutStrategy.class));
        internal.setLoggerSink(loggerSink);

        ssoService = new SSOServiceImpl(internal, hotpService, null, null, null, loggerSink);
        ssoService.setSubsystemIdentifierObtainer(hint -> CALLER);
    }

    private void expectOwnUserWithoutKey() {
        UserForDescription stored = new UserForDescription();
        stored.setUsername(OWN);
        stored.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        UserWithStatus status = new UserWithStatus();
        status.setUserName(OWN);
        expect(userService.isUserAccessibleFrom(OWN, CALLER)).andReturn(true);
        expect(userService.getUserForDescription(OWN)).andReturn(stored).anyTimes();
        expect(userService.getUserStatuses(OWN)).andReturn(Collections.singletonList(status)).anyTimes();
        expect(userService.getOtpMasterKeyByUsername(OWN)).andReturn(null).anyTimes();
        loggerSink.info(anyObject(org.slf4j.Logger.class), eq("REMOTE_OTP_CHECK"), eq(false), eq(OWN),
                eq("status=INVALID, reason=no key"));
    }

    private void expectForeignUser() {
        // only the access check reaches the data
        expect(userService.isUserAccessibleFrom(FOREIGN, CALLER)).andReturn(false);
        loggerSink.info(anyObject(org.slf4j.Logger.class), eq("SUBSYSTEM_ACCESS_DENIED"), eq(false), eq(FOREIGN),
                eq("method=checkOtp, subsystem=" + CALLER));
    }

    @Test
    public void foreignUserGetsTheAnswerOfOwnUserWithoutKey() {
        expectOwnUserWithoutKey();
        expectForeignUser();
        replay(userService, loggerSink);

        CheckOtpResult own = ssoService.checkOtp(new CheckOtpRequest(OWN, CODE, OTPType.GOOGLE_AUTH, false));
        CheckOtpResult foreign = ssoService.checkOtp(new CheckOtpRequest(FOREIGN, CODE, OTPType.GOOGLE_AUTH, false));

        assertEquals(CheckOtpResult.Status.INVALID, own.getStatus());
        assertEquals(own, foreign);
        verify(userService, loggerSink);
    }
}
