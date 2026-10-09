package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SSOSetupGoogleAuthPageTest extends AbstractPageTest {
    private UserService userService;
    private HOTPService hotpService;
    private SessionService sessionService;
    private SettingsService settingsService;
    private SubsystemService subsystemService;
    private InternalSSOService internalSSOService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        hotpService = EasyMock.createStrictMock(HOTPService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        settingsService = EasyMock.createStrictMock(SettingsService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        internalSSOService = EasyMock.createStrictMock(InternalSSOService.class);
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
        if (type == UserService.class) {
            return userService;
        }
        if (type == HOTPService.class) {
            return hotpService;
        }
        if (type == SessionService.class) {
            return sessionService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == InternalSSOService.class) {
            return internalSSOService;
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    private SSOLoginData loginData(boolean setupRequired) {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setSubsystemTitle("The Subsystem (tm)");
        loginData.setUsername("known-user");
        loginData.setGoogleAuthSetupRequired(setupRequired);
        return loginData;
    }

    private String shownSecret() {
        Label key = (Label) tester.getLastRenderedPage().get("form:otp:refreshable:key");
        return (String) key.getDefaultModelObject();
    }

    private String validCode() {
        return String.format("%06d", new GoogleAuthenticator().getTotpPassword(shownSecret()));
    }

    private String invalidCode() {
        String valid = validCode();
        return "000000".equals(valid) ? "111111" : "000000";
    }

    @Test
    public void testUserWithConfiguredKeyCannotOpenSetupAfterPassword() {
        // state left by SSOLoginPasswordPage for a user who already has a key
        replay(userService, hotpService, csrfValidator, subsystemService);

        tester.getSession().setSsoLoginData(loginData(false));
        tester.startPage(SSOSetupGoogleAuthPage.class);

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        verify(userService, hotpService);
    }

    @Test
    public void testSetupWithoutLoginDataRejected() {
        replay(userService, hotpService, csrfValidator, subsystemService);

        tester.startPage(SSOSetupGoogleAuthPage.class);

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        verify(userService, hotpService);
    }

    @Test
    public void testPasswordStepClearsStaleFlagForUserWithKey() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        UserForDescription description = EasyMock.createNiceMock(UserForDescription.class);
        expect(description.getOtpType()).andReturn(OTPType.GOOGLE_AUTH).anyTimes();
        expect(description.isOtpOptional()).andReturn(false).anyTimes();
        expect(userService.getUserForDescription("known-user")).andReturn(description).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        replay(description, userService, hotpService, csrfValidator, subsystemService, settingsService);

        SSOLoginData loginData = loginData(true);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        assertFalse(loginData.isGoogleAuthSetupRequired());
        verify(hotpService);
    }

    @Test
    public void testPasswordStepSetsFlagForUserWithoutKey() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        UserForDescription description = EasyMock.createNiceMock(UserForDescription.class);
        expect(description.getOtpType()).andReturn(OTPType.GOOGLE_AUTH).anyTimes();
        expect(description.isOtpOptional()).andReturn(false).anyTimes();
        expect(userService.getUserForDescription("known-user")).andReturn(description).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        replay(description, userService, hotpService, csrfValidator, subsystemService, settingsService);

        SSOLoginData loginData = loginData(false);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();

        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);
        assertTrue(loginData.isGoogleAuthSetupRequired());
        verify(hotpService);
    }

    @Test
    public void testSetupDoesNotSaveKeyWithInvalidCode() {
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        replay(userService, hotpService, csrfValidator, subsystemService);

        SSOLoginData loginData = loginData(true);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOSetupGoogleAuthPage.class);
        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);

        FormTester form = tester.newFormTester("form");
        form.setValue("code", invalidCode());
        form.submit();

        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);
        tester.assertLabel("form:message", "One-time password value did not match.");
        assertTrue(loginData.isGoogleAuthSetupRequired());
        verify(hotpService);
    }

    @Test
    public void testStateEchoedAfterSetupAndOtp() throws Exception {
        String state = "Ab0_-cdefghijklmnopqrstuvwxyz0123456789ABCDE";
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        hotpService.persistOtpKey(eq(OTPType.GOOGLE_AUTH), eq("known-user"), anyString());
        expect(hotpService.validateGoogleTimePassword(eq("known-user"), anyString())).andReturn(CheckOtpResult.Status.SUCCESS);
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "123456"))
                .andReturn(CheckOtpResult.Status.SUCCESS);
        expect(sessionService.createSSOSession("known-user")).andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(userService, hotpService, internalSSOService, sessionService, csrfValidator, subsystemService);

        SSOLoginData loginData = loginData(true);
        loginData.setState(state);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOSetupGoogleAuthPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("code", validCode());
        form.submit();
        tester.assertRenderedPage(SSOLoginHOTPPage.class);

        FormTester otpForm = tester.newFormTester("form");
        otpForm.setValue("hotp", "123456");
        otpForm.submit();
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget&state=" + state);

        verify(hotpService, internalSSOService, sessionService, subsystemService);
    }

    @Test
    public void testSetupSavesKeyWithValidCode() throws Exception {
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        hotpService.persistOtpKey(eq(OTPType.GOOGLE_AUTH), eq("known-user"), anyString());
        // records the confirmation code's step (anti-replay), strict order: after the key is saved
        Capture<String> recordedCode = newCapture();
        expect(hotpService.validateGoogleTimePassword(eq("known-user"), capture(recordedCode))).andReturn(CheckOtpResult.Status.SUCCESS);
        replay(userService, hotpService, csrfValidator, subsystemService);

        SSOLoginData loginData = loginData(true);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOSetupGoogleAuthPage.class);

        FormTester form = tester.newFormTester("form");
        String code = validCode();
        form.setValue("code", code);
        form.submit();

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        assertFalse(loginData.isGoogleAuthSetupRequired());
        assertEquals(code, recordedCode.getValue());
        verify(hotpService);
    }

    @Test
    public void testKeyEnrolledMeanwhileIsNotOverwritten() {
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("encrypted-key").anyTimes();
        replay(userService, hotpService, csrfValidator, subsystemService);

        SSOLoginData loginData = loginData(true);
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOSetupGoogleAuthPage.class);

        FormTester form = tester.newFormTester("form");
        form.setValue("code", validCode());
        form.submit();

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        assertFalse(loginData.isGoogleAuthSetupRequired());
        verify(hotpService);
    }
}
