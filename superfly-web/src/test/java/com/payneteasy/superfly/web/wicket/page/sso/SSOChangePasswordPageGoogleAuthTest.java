package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
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
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A temporary password of a Google Auth user leads to the second factor first (the code for an existing key,
 * the setup otherwise); the password is changed after it.
 */
public class SSOChangePasswordPageGoogleAuthTest extends AbstractPageTest {
    private UserService userService;
    private HOTPService hotpService;
    private SettingsService settingsService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        hotpService = EasyMock.createStrictMock(HOTPService.class);
        settingsService = EasyMock.createStrictMock(SettingsService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        csrfValidator = EasyMock.createMock(CsrfValidator.class);

        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        expect(subsystemService.getSubsystemByName("test-subsystem")).andReturn(subsystem).anyTimes();
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();

        expect(userService.checkUserCanLoginWithThisPassword("user", "temp-password", "test-subsystem"))
                .andReturn(UserLoginStatus.TEMP_PASSWORD);
        UserForDescription user = new UserForDescription();
        user.setUsername("user");
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        user.setOtpOptional(false);
        expect(userService.getUserForDescription("user")).andReturn(user);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == UserService.class) {
            return userService;
        }
        if (type == HOTPService.class) {
            return hotpService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == SessionService.class) {
            return EasyMock.createStrictMock(SessionService.class);
        }
        if (type == InternalSSOService.class) {
            return EasyMock.createStrictMock(InternalSSOService.class);
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    private SSOLoginData submitTemporaryPassword() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setSubsystemTitle("The Subsystem (tm)");
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "user");
        form.setValue("password", "temp-password");
        form.submit();
        return loginData;
    }

    @Test
    public void testExistingKeyIsNotOfferedForReplacement() {
        expect(userService.getOtpMasterKeyByUsername("user")).andReturn("encrypted-key");
        replay(userService, hotpService, settingsService, subsystemService, csrfValidator);

        SSOLoginData loginData = submitTemporaryPassword();

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        assertFalse(loginData.isGoogleAuthSetupRequired());
        assertTrue(loginData.isPasswordChangeRequired());
        assertFalse(loginData.isPasswordChangeAllowed());
        verify(userService, hotpService);
    }

    @Test
    public void testMissingKeyLeadsToSetup() {
        expect(userService.getOtpMasterKeyByUsername("user")).andReturn(null);
        replay(userService, hotpService, settingsService, subsystemService, csrfValidator);

        SSOLoginData loginData = submitTemporaryPassword();

        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);
        assertTrue(loginData.isGoogleAuthSetupRequired());
        assertTrue(loginData.isPasswordChangeRequired());
        assertFalse(loginData.isPasswordChangeAllowed());
        verify(userService, hotpService);
    }
}
