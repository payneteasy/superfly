package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SSOChangePasswordPageGoogleAuthTest extends AbstractPageTest {
    private UserService userService;
    private HOTPService hotpService;
    private SettingsService settingsService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() throws PolicyValidationException {
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

        expect(settingsService.getPolicy()).andReturn(Policy.NONE);
        userService.validatePassword("user", "password");
        userService.changeTempPassword("user", "password");
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

    private SSOLoginData changePassword() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setSubsystemTitle("The Subsystem (tm)");
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(new SSOChangePasswordPage("user"));
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "password");
        form.setValue("password2", "password");
        form.submit();
        return loginData;
    }

    @Test
    public void testExistingKeyIsNotOfferedForReplacement() {
        expect(userService.getOtpMasterKeyByUsername("user")).andReturn("encrypted-key");
        replay(userService, hotpService, settingsService, subsystemService, csrfValidator);

        SSOLoginData loginData = changePassword();

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        assertFalse(loginData.isGoogleAuthSetupRequired());
        verify(userService, hotpService);
    }

    @Test
    public void testMissingKeyLeadsToSetup() {
        expect(userService.getOtpMasterKeyByUsername("user")).andReturn(null);
        replay(userService, hotpService, settingsService, subsystemService, csrfValidator);

        SSOLoginData loginData = changePassword();

        tester.assertRenderedPage(SSOSetupGoogleAuthPage.class);
        assertTrue(loginData.isGoogleAuthSetupRequired());
        verify(userService, hotpService);
    }
}
