package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * A configured OTP key makes the code mandatory even when the user is flagged as OTP-optional.
 */
public class SSOLoginPasswordPageOtpKeyTest extends AbstractPageTest {
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        settingsService = EasyMock.createStrictMock(SettingsService.class);
        csrfValidator = EasyMock.createStrictMock(CsrfValidator.class);

        expect(subsystemService.getSubsystemByName("test-subsystem"))
                        .andReturn(createSubsystem()).anyTimes();

        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == UserService.class) {
            return userService;
        }
        if (type == SessionService.class) {
            return sessionService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    private UISubsystem createSubsystem() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        return subsystem;
    }

    private UserForDescription optionalGoogleAuthUser() {
        UserForDescription userForDescription = EasyMock.createNiceMock(UserForDescription.class);
        expect(userForDescription.isOtpOptional()).andReturn(Boolean.TRUE).anyTimes();
        expect(userForDescription.getOtpType()).andReturn(OTPType.GOOGLE_AUTH).anyTimes();
        return userForDescription;
    }

    @Test
    public void testOptionalWithKeyAsksForHOTP() {
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("1234").anyTimes();
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        UserForDescription userForDescription = optionalGoogleAuthUser();
        expect(userService.getUserForDescription("known-user")).andReturn(userForDescription).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("123").anyTimes();
        replay(userForDescription, userService, sessionService, settingsService, subsystemService, csrfValidator);

        submit();

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        verify(sessionService);
    }

    @Test
    public void testOptionalWithoutKeyLogsInWithoutOtp() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        UserForDescription userForDescription = optionalGoogleAuthUser();
        expect(userService.getUserForDescription("known-user")).andReturn(userForDescription).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn(null).anyTimes();
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(userForDescription, userService, sessionService, settingsService, subsystemService, csrfValidator);

        submit();

        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        verify(sessionService);
    }

    private void submit() {
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
    }
}
