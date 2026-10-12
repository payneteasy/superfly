package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/** The OTP page tells why a code was refused; the success path and the attempt limit are as before. */
public class SSOLoginHOTPPageFailureMessagesTest extends AbstractPageTest {
    private static final String INVALID_MESSAGE = "One-time password value did not match.";
    private static final String ALREADY_USED_MESSAGE =
            "This code has already been used. Wait for the next code and enter it.";
    private static final String CLOCK_SKEW_MESSAGE = "The time on your device is out of sync with the server. "
            + "Synchronize the time on your device and enter a new code.";
    private static final String LOCKED_MESSAGE = "The account is locked. Contact your administrator.";
    private static final String TOO_MANY_ATTEMPTS_MESSAGE = "Too many failed login attempts. Try again later.";
    private static final int PAIR_LIMIT = 5;

    private InternalSSOService internalSSOService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        internalSSOService = EasyMock.createStrictMock(InternalSSOService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        // not strict: the attempt limit test opens the page again after a form was submitted
        csrfValidator = EasyMock.createMock(CsrfValidator.class);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        expect(subsystemService.getSubsystemByName("test-subsystem")).andReturn(subsystem).anyTimes();
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
    }

    @Override
    protected Object getBean(Class<?> type) {
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
    public void invalidCodeShowsTheGenericMessage() {
        assertMessageAfterRefusedCode(CheckOtpResult.Status.INVALID, INVALID_MESSAGE);
    }

    @Test
    public void alreadyUsedCodeAsksToWaitForTheNextCode() {
        assertMessageAfterRefusedCode(CheckOtpResult.Status.ALREADY_USED, ALREADY_USED_MESSAGE);
    }

    @Test
    public void clockSkewAsksToSynchronizeTheDeviceTime() {
        assertMessageAfterRefusedCode(CheckOtpResult.Status.CLOCK_SKEW, CLOCK_SKEW_MESSAGE);
    }

    @Test
    public void lockedAccountAsksToContactTheAdministrator() {
        assertMessageAfterRefusedCode(CheckOtpResult.Status.LOCKED, LOCKED_MESSAGE);
    }

    @Test
    public void unknownResultShowsTheGenericMessage() {
        assertMessageAfterRefusedCode(null, INVALID_MESSAGE);
    }

    @Test
    public void validCodeContinuesToTheSubsystem() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "111111"))
                .andReturn(CheckOtpResult.Status.SUCCESS);
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replayAll();

        submitCode("111111");

        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");
        verify(internalSSOService, sessionService, subsystemService, csrfValidator);
    }

    @Test
    public void everyRefusedCodeCountsTowardTheAttemptLimit() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "222222"))
                .andReturn(CheckOtpResult.Status.INVALID)
                .andReturn(CheckOtpResult.Status.ALREADY_USED)
                .andReturn(CheckOtpResult.Status.CLOCK_SKEW)
                .andReturn(CheckOtpResult.Status.LOCKED)
                .andReturn(CheckOtpResult.Status.INVALID);
        replayAll();

        for (int i = 0; i < PAIR_LIMIT; i++) {
            submitCode("222222");
        }
        // the strict mock fails on a sixth call
        submitCode("222222");

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        tester.assertVisible("form:message");
        tester.assertLabel("form:message", TOO_MANY_ATTEMPTS_MESSAGE);
        verify(internalSSOService, csrfValidator);
    }

    private void assertMessageAfterRefusedCode(CheckOtpResult.Status status, String expectedMessage) {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "222222"))
                .andReturn(status);
        replayAll();

        submitCode("222222");

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        tester.assertVisible("form:message");
        tester.assertLabel("form:message", expectedMessage);
        verify(internalSSOService, csrfValidator);
    }

    private void replayAll() {
        replay(internalSSOService, sessionService, subsystemService, csrfValidator);
    }

    private void submitCode(String code) {
        // the OTP page is shown only for a user who has passed the password step: its login data has the username
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setUsername("known-user");
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", code);
        form.submit();
    }
}
