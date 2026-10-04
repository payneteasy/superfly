package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A configured OTP master key makes the code mandatory even for a user flagged as OTP-optional.
 */
public class InternalSSOServiceOtpRequiredTest {

    private static final String USER = "user";

    private UserService userService;
    private HOTPService hotpService;
    private InternalSSOServiceImpl service;

    @Before
    public void setUp() {
        userService = EasyMock.createNiceMock(UserService.class);
        hotpService = EasyMock.createNiceMock(HOTPService.class);
        service = new InternalSSOServiceImpl();
        service.setUserService(userService);
        service.setHotpService(hotpService);
        service.setLoggerSink(EasyMock.createNiceMock(LoggerSink.class));
        service.setLockoutStrategy(EasyMock.createNiceMock(LockoutStrategy.class));
    }

    @Test
    public void optionalWithKeyAndEmptyCodeIsRejected() throws Exception {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn("encrypted-key").anyTimes();
        EasyMock.expect(hotpService.validateGoogleTimePassword(USER, "")).andReturn(false);
        userService.incrementHOTPLoginsFailed(USER);
        EasyMock.replay(userService, hotpService);

        assertFalse(service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, ""));
        assertFalse(service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, null));
        EasyMock.verify(userService);
    }

    @Test
    public void optionalWithoutKeyAndEmptyCodeSucceeds() {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(null).anyTimes();
        EasyMock.replay(userService, hotpService);

        assertTrue(service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, ""));
        assertTrue(service.checkOtp(OTPType.GOOGLE_AUTH, true, USER, null));
    }

    @Test
    public void ssoUserOfOptionalUserWithKeyIsNotOptional() {
        EasyMock.expect(userService.pseudoAuthenticate(USER, "sub")).andReturn(session(true));
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn("encrypted-key").anyTimes();
        EasyMock.replay(userService);

        SSOUser ssoUser = service.pseudoAuthenticate(USER, "sub");

        assertFalse(ssoUser.isOtpOptional());
    }

    @Test
    public void ssoUserOfOptionalUserWithoutKeyStaysOptional() {
        EasyMock.expect(userService.pseudoAuthenticate(USER, "sub")).andReturn(session(true));
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(null).anyTimes();
        EasyMock.replay(userService);

        SSOUser ssoUser = service.pseudoAuthenticate(USER, "sub");

        assertTrue(ssoUser.isOtpOptional());
    }

    private static AuthSession session(boolean optional) {
        AuthSession session = new AuthSession();
        session.setUsername(USER);
        session.setSessionId(1L);
        session.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        session.setOtpOptional(optional);
        return session;
    }
}
