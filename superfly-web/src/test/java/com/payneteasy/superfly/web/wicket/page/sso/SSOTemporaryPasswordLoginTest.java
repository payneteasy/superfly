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
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.Cookie;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/**
 * SSO login with a temporary password: the second factor is asked as after a regular password, then the
 * password is changed and the token is issued, as in the admin console. The change page belongs to such
 * a login only, once its second factor has been passed.
 */
public class SSOTemporaryPasswordLoginTest {
    private static final String STATE = "Ab0_-cdefghijklmnopqrstuvwxyz0123456789ABCDE";
    private static final String REDIRECT_WITH_STATE =
            "http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget&state=" + STATE;
    private static final String NO_LOGIN_DATA = "No login data found";
    private static final String INVALID_MESSAGE = "One-time password value did not match.";
    private static final String ALREADY_USED_MESSAGE =
            "This code has already been used. Wait for the next code and enter it.";
    private static final String CLOCK_SKEW_MESSAGE = "The time on your device is out of sync with the server. "
            + "Synchronize the time on your device and enter a new code.";
    private static final String LOCKED_MESSAGE = "The account is locked. Contact your administrator.";
    private static final String TOO_MANY_ATTEMPTS_MESSAGE = "Too many failed login attempts. Try again later.";

    private final Map<Class<?>, Object> beans = new HashMap<>();
    private ContainerLikeTester tester;
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private InternalSSOService internalSSOService;
    private HOTPService hotpService;
    private SettingsService settingsService;

    @Before
    public void setUp() {
        LoginAttemptLimiter.install(LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP);
        userService = createMock(UserService.class);
        sessionService = createMock(SessionService.class);
        subsystemService = createMock(SubsystemService.class);
        internalSSOService = createMock(InternalSSOService.class);
        hotpService = createMock(HOTPService.class);
        settingsService = createNiceMock(SettingsService.class);
        beans.put(UserService.class, userService);
        beans.put(SessionService.class, sessionService);
        beans.put(SubsystemService.class, subsystemService);
        beans.put(InternalSSOService.class, internalSSOService);
        beans.put(HOTPService.class, hotpService);
        beans.put(SettingsService.class, settingsService);
        beans.put(CsrfValidator.class, new CsrfValidatorImpl(true));

        tester = new ContainerLikeTester(beans);
        expect(subsystemService.getSubsystemByName(anyString())).andReturn(null).anyTimes();
        expect(settingsService.getPolicy()).andReturn(Policy.NONE).anyTimes();
    }

    @Test
    public void testOtpCodeIsAskedBeforeThePasswordChange() throws Exception {
        expectTemporaryPassword(googleAuthUser(false));
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        expectOtpCode();
        expectPasswordChange();
        expectToken();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOLoginHOTPPage.class);

        submitOtp("111111");
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        submitNewPassword();
        tester.assertRedirectUrl(REDIRECT_WITH_STATE);
        assertSsoSessionCookie();
        verifyAll();
    }

    @Test
    public void testGoogleAuthIsSetUpBeforeThePasswordChange() throws Exception {
        expectTemporaryPassword(googleAuthUser(false));
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        hotpService.persistOtpKey(eq(OTPType.GOOGLE_AUTH), eq("known-user"), anyString());
        expect(hotpService.validateGoogleTimePassword(eq("known-user"), anyString()))
                .andReturn(CheckOtpResult.Status.SUCCESS);
        expectPasswordChange();
        expectToken();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);

        FormTester setupForm = tester.newFormTester("form");
        setupForm.setValue("code", setupConfirmationCode());
        tester.submitWithCsrf(setupForm);
        // the code that confirms the key is the second factor: no other code is asked before the password change
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        submitNewPassword();
        tester.assertRedirectUrl(REDIRECT_WITH_STATE);
        assertSsoSessionCookie();
        verifyAll();
    }

    @Test
    public void testUserWithoutOtpChangesThePasswordRightAway() throws Exception {
        expectTemporaryPassword(userWithoutOtp());
        expectPasswordChange();
        expectToken();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        submitNewPassword();
        tester.assertRedirectUrl(REDIRECT_WITH_STATE);
        assertSsoSessionCookie();
        verifyAll();
    }

    @Test
    public void testUserWithOptionalOtpAndNoKeyChangesThePasswordRightAway() throws Exception {
        expectTemporaryPassword(googleAuthUser(true));
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        expectPasswordChange();
        expectToken();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        submitNewPassword();
        tester.assertRedirectUrl(REDIRECT_WITH_STATE);
        verifyAll();
    }

    @Test
    public void testChangePageIsRefusedBeforeTheSecondFactor() {
        expectTemporaryPassword(googleAuthUser(false));
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOLoginHOTPPage.class);

        assertChangePageRefused();
        verifyAll();
    }

    @Test
    public void testChangePageIsRefusedWithoutALogin() {
        replayAll();

        assertChangePageRefused();
        verifyAll();
    }

    @Test
    public void testChangePageIsRefusedAfterARegularPassword() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(userService.getUserForDescription("known-user")).andReturn(googleAuthUser(false)).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        replayAll();

        startLogin();
        submitPassword("password");
        tester.assertRenderedPage(SSOLoginHOTPPage.class);

        assertChangePageRefused();
        verifyAll();
    }

    @Test
    public void testPasswordIsNotChangedOnceTheLoginHasRestarted() throws Exception {
        expectTemporaryPassword(userWithoutOtp());
        // the form validators run before the page checks the login
        userService.validatePassword("known-user", "new-password");
        expectLastCall();
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        // a login started in another tab replaces the login data of the session
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        submitNewPassword();

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", NO_LOGIN_DATA);
        verifyAll();
    }

    @Test
    public void testRefusedOtpCodeDoesNotOpenThePasswordChange() {
        expectTemporaryPassword(googleAuthUser(false));
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "222222"))
                .andReturn(CheckOtpResult.Status.INVALID)
                .andReturn(CheckOtpResult.Status.ALREADY_USED)
                .andReturn(CheckOtpResult.Status.CLOCK_SKEW)
                .andReturn(CheckOtpResult.Status.LOCKED)
                .andReturn(CheckOtpResult.Status.INVALID);
        replayAll();

        startLogin();
        submitPassword("temp-password");
        tester.assertRenderedPage(SSOLoginHOTPPage.class);

        for (String message : List.of(INVALID_MESSAGE, ALREADY_USED_MESSAGE, CLOCK_SKEW_MESSAGE, LOCKED_MESSAGE,
                INVALID_MESSAGE)) {
            submitOtp("222222");
            tester.assertRenderedPage(SSOLoginHOTPPage.class);
            tester.assertLabel("form:message", message);
        }
        // the attempt limit stops the next code before it is checked, as after a regular password
        submitOtp("222222");
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        tester.assertLabel("form:message", TOO_MANY_ATTEMPTS_MESSAGE);

        assertChangePageRefused();
        verifyAll();
    }

    /** The change page has no URL of its own: it is built the way the login pages build it. */
    private void assertChangePageRefused() {
        RestartResponseException refused = assertThrows(RestartResponseException.class,
                () -> new SSOChangePasswordPage("known-user"));
        tester.processRequest(refused.getReplacementRequestHandler());
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", NO_LOGIN_DATA);
    }

    private void assertSsoSessionCookie() {
        Cookie cookie = tester.getLastResponse().getCookies().stream()
                .filter(c -> SSOUtils.SSO_SESSION_ID_COOKIE_NAME.equals(c.getName()))
                .findAny()
                .orElse(null);
        assertNotNull("the SSO session cookie must be set", cookie);
        assertEquals("sso-session-id", cookie.getValue());
    }

    private void expectTemporaryPassword(UserForDescription user) {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "temp-password", "test-subsystem"))
                .andReturn(UserLoginStatus.TEMP_PASSWORD);
        expect(userService.getUserForDescription("known-user")).andReturn(user).anyTimes();
    }

    private void expectOtpCode() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "111111"))
                .andReturn(CheckOtpResult.Status.SUCCESS);
    }

    private void expectPasswordChange() throws Exception {
        userService.validatePassword("known-user", "new-password");
        expectLastCall();
        userService.changeTempPassword("known-user", "new-password");
        expectLastCall();
    }

    private void expectToken() {
        expect(sessionService.createSSOSession("known-user")).andReturn(new SSOSession(1L, "sso-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
    }

    private void replayAll() {
        replay(userService, sessionService, subsystemService, internalSSOService, hotpService, settingsService);
    }

    private void verifyAll() {
        verify(userService, sessionService, subsystemService, internalSSOService, hotpService);
    }

    private void startLogin() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setSubsystemTitle("The Subsystem (tm)");
        loginData.setState(STATE);
        tester.getSession().bind();
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
    }

    private void submitPassword(String password) {
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", password);
        tester.submitWithCsrf(form);
    }

    private void submitOtp(String code) {
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", code);
        tester.submitWithCsrf(form);
    }

    private void submitNewPassword() {
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "new-password");
        form.setValue("password2", "new-password");
        tester.submitWithCsrf(form);
    }

    /** The code that the authenticator app would show for the key on the setup page. */
    private String setupConfirmationCode() {
        Label key = (Label) tester.getLastRenderedPage().get("form:otp:refreshable:key");
        return String.format("%06d", new GoogleAuthenticator().getTotpPassword((String) key.getDefaultModelObject()));
    }

    private static UserForDescription googleAuthUser(boolean otpOptional) {
        UserForDescription user = new UserForDescription();
        user.setUsername("known-user");
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        user.setOtpOptional(otpOptional);
        return user;
    }

    private static UserForDescription userWithoutOtp() {
        UserForDescription user = new UserForDescription();
        user.setUsername("known-user");
        user.setOtpTypeCode(OTPType.NONE.code());
        return user;
    }
}
