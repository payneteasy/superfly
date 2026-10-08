package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/** The SSO pages bypass /j_superfly_* filters, so they throttle failed attempts themselves. */
public class SSOLoginRateLimitTest extends AbstractPageTest {
    private static final int PAIR_LIMIT = 5;

    private UserService        userService;
    private InternalSSOService internalSSOService;
    private SessionService     sessionService;
    private SubsystemService   subsystemService;
    private CsrfValidator      csrfValidator;

    @Before
    public void setUp() {
        LoginAttemptLimiter.install(LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP);
        userService = EasyMock.createStrictMock(UserService.class);
        internalSSOService = EasyMock.createStrictMock(InternalSSOService.class);
        sessionService = EasyMock.createNiceMock(SessionService.class);
        subsystemService = EasyMock.createNiceMock(SubsystemService.class);
        csrfValidator = EasyMock.createNiceMock(CsrfValidator.class);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        expect(subsystemService.getSubsystemByName("test-subsystem")).andReturn(subsystem).anyTimes();
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
        replay(sessionService, subsystemService, csrfValidator);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == UserService.class) {
            return userService;
        }
        if (type == InternalSSOService.class) {
            return internalSSOService;
        }
        if (type == SessionService.class) {
            return sessionService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void passwordStepIsBlockedWithoutCallingUserService() {
        expect(userService.checkUserCanLoginWithThisPassword(anyString(), eq("bad"), eq("test-subsystem")))
                .andReturn(UserLoginStatus.FAILED).times(PAIR_LIMIT);
        replay(userService);

        for (int i = 0; i < PAIR_LIMIT; i++) {
            submitPassword(i % 2 == 0 ? "victim" : "VICTIM ");
        }
        // the strict mock fails on an unexpected sixth call
        submitPassword("victim");

        tester.assertContains("Too many failed login attempts");
        verify(userService);
    }

    @Test
    public void otpStepIsBlockedWithoutCallingAuthenticationService() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "victim", "123456"))
                .andReturn(CheckOtpResult.Status.INVALID).times(PAIR_LIMIT);
        replay(internalSSOService);

        for (int i = 0; i < PAIR_LIMIT; i++) {
            submitOtp();
        }
        submitOtp();

        tester.assertContains("Too many failed login attempts");
        verify(internalSSOService);
    }

    private void submitPassword(String username) {
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", username);
        form.setValue("password", "bad");
        form.submit();
    }

    private void submitOtp() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setUsername("victim");
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", "123456");
        form.submit();
    }
}
