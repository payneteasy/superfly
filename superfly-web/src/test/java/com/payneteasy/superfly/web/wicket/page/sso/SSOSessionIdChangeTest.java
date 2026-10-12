package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotEquals;

/**
 * The HTTP session id changes after the password step (also when it accepts a temporary password), after the
 * second factor that precedes a password change and after the password change, while the following forms still
 * work. The pages run in a {@link ContainerLikeTester}: the session keeps its attributes when its id changes.
 */
public class SSOSessionIdChangeTest {

    private static final String LANDING = "http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget";

    private final Map<Class<?>, Object> beans = new HashMap<>();
    private ContainerLikeTester tester;
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private InternalSSOService internalSSOService;
    private SettingsService settingsService;

    @Before
    public void setUp() {
        LoginAttemptLimiter.install(LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP);
        userService = createMock(UserService.class);
        sessionService = createMock(SessionService.class);
        subsystemService = createMock(SubsystemService.class);
        internalSSOService = createMock(InternalSSOService.class);
        settingsService = createNiceMock(SettingsService.class);
        beans.put(UserService.class, userService);
        beans.put(SessionService.class, sessionService);
        beans.put(SubsystemService.class, subsystemService);
        beans.put(InternalSSOService.class, internalSSOService);
        beans.put(SettingsService.class, settingsService);
        beans.put(CsrfValidator.class, new CsrfValidatorImpl(true));

        tester = new ContainerLikeTester(beans);
        expect(subsystemService.getSubsystemByName(anyString())).andReturn(null).anyTimes();
        expect(settingsService.getPolicy()).andReturn(Policy.NONE).anyTimes();
    }

    @Test
    public void testSessionIdChangesAfterPasswordAndOtpFormStillWorks() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(userService.getUserForDescription("known-user")).andReturn(googleAuthUser()).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("key").anyTimes();
        expectOtp();
        expectToken();
        replay(userService, sessionService, subsystemService, internalSSOService, settingsService);

        startWithLoginData(SSOLoginPasswordPage.class);
        String idBeforePassword = tester.getHttpSession().getId();
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        tester.submitWithCsrf(form);

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        String idAfterPassword = tester.getHttpSession().getId();
        assertNotEquals("session id must change after the password step", idBeforePassword, idAfterPassword);

        submitOtpAndAssertRedirect(idAfterPassword);
        verify(userService, sessionService, subsystemService, internalSSOService);
    }

    @Test
    public void testSessionIdChangesAfterPasswordChangeAndNoOtpIsAskedAgain() throws Exception {
        userService.validatePassword("known-user", "new-password");
        expectLastCall();
        userService.changeTempPassword("known-user", "new-password");
        expectLastCall();
        expectToken();
        replay(userService, sessionService, subsystemService, internalSSOService, settingsService);

        tester.getSession().bind();
        tester.getSession().setSsoLoginData(loginDataAfterSecondFactor());
        tester.startPage(new SSOChangePasswordPage("known-user"));
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        String idBeforeChange = tester.getHttpSession().getId();
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "new-password");
        form.setValue("password2", "new-password");
        tester.submitWithCsrf(form);

        // the second factor has been passed before the change: the token is issued without another OTP form
        tester.assertRedirectUrl(LANDING);
        assertNotEquals("session id must change after the password change", idBeforeChange,
                tester.getHttpSession().getId());
        verify(userService, sessionService, subsystemService, internalSSOService);
    }

    @Test
    public void testSessionIdChangesAfterTemporaryPasswordOtpAndPasswordChange() throws Exception {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "temp-password", "test-subsystem"))
                .andReturn(UserLoginStatus.TEMP_PASSWORD);
        expect(userService.getUserForDescription("known-user")).andReturn(googleAuthUser()).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("key").anyTimes();
        expectOtp();
        userService.validatePassword("known-user", "new-password");
        expectLastCall();
        userService.changeTempPassword("known-user", "new-password");
        expectLastCall();
        expectToken();
        replay(userService, sessionService, subsystemService, internalSSOService, settingsService);

        startWithLoginData(SSOLoginPasswordPage.class);
        String idBeforePassword = tester.getHttpSession().getId();
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "temp-password");
        tester.submitWithCsrf(form);

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        String idAfterTempPassword = tester.getHttpSession().getId();
        assertNotEquals("session id must change after the temporary password is accepted",
                idBeforePassword, idAfterTempPassword);

        FormTester otpForm = tester.newFormTester("form");
        otpForm.setValue("hotp", "111111");
        tester.submitWithCsrf(otpForm);

        tester.assertRenderedPage(SSOChangePasswordPage.class);
        String idAfterOtp = tester.getHttpSession().getId();
        assertNotEquals("session id must change after the second factor", idAfterTempPassword, idAfterOtp);

        FormTester changeForm = tester.newFormTester("change-password-panel:form");
        changeForm.setValue("password", "new-password");
        changeForm.setValue("password2", "new-password");
        tester.submitWithCsrf(changeForm);

        tester.assertRedirectUrl(LANDING);
        assertNotEquals("session id must change after the password change", idAfterOtp,
                tester.getHttpSession().getId());
        verify(userService, sessionService, subsystemService, internalSSOService);
    }

    private void expectOtp() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "111111"))
                .andReturn(CheckOtpResult.Status.SUCCESS);
    }

    private void expectToken() {
        expect(sessionService.createSSOSession("known-user")).andReturn(new SSOSession(1L, "sso-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
    }

    private void submitOtpAndAssertRedirect(String idBeforeOtp) {
        FormTester otpForm = tester.newFormTester("form");
        otpForm.setValue("hotp", "111111");
        tester.submitWithCsrf(otpForm);
        tester.assertRedirectUrl(LANDING);
        assertNotEquals("session id must change before the token is issued", idBeforeOtp,
                tester.getHttpSession().getId());
    }

    private void startWithLoginData(Class<? extends BaseSSOPage> pageClass) {
        tester.getSession().bind();
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(pageClass);
        tester.assertRenderedPage(pageClass);
    }

    /** The state left by the second factor of a login that has accepted a temporary password. */
    private static SSOLoginData loginDataAfterSecondFactor() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setUsername("known-user");
        loginData.setPasswordChangeRequired(true);
        loginData.setPasswordChangeAllowed(true);
        return loginData;
    }

    private static UserForDescription googleAuthUser() {
        UserForDescription user = new UserForDescription();
        user.setUsername("known-user");
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        user.setOtpOptional(false);
        return user;
    }
}
