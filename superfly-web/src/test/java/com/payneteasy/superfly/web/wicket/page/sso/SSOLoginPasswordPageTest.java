package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

/**
 * @author rpuch
 */
public class SSOLoginPasswordPageTest extends AbstractPageTest {
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;

    @Before
    public void setUp() {
        userService = EasyMock.createStrictMock(UserService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        settingsService = EasyMock.createStrictMock(SettingsService.class);

        expect(subsystemService.getSubsystemByName("test-subsystem"))
                        .andReturn(createSubsystem()).anyTimes();
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
        return super.getBean(type);
    }

    private UISubsystem createSubsystem() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        return subsystem;
    }

    @Test
    public void testNoLoginData() {
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }

    @Test
    public void testSuccessNoHOTP() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        expect(settingsService.getPolicy()).andReturn(Policy.NONE);
        expect(settingsService.isHotpDisabled()).andReturn(true).anyTimes();
        replay(userService, sessionService, subsystemService, settingsService);

        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        verify(userService, sessionService, subsystemService, settingsService);
    }

    @Test
    public void testSuccessAndHOTP() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(settingsService.getPolicy()).andReturn(Policy.PCIDSS);
        expect(settingsService.isHotpDisabled()).andReturn(false);
        replay(userService, settingsService, subsystemService);

        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        assertEquals("known-user", tester.getSession().getSsoLoginData().getUsername());

        verify(userService, settingsService, subsystemService);
    }

    @Test
    public void testFailure() {
        expect(userService.checkUserCanLoginWithThisPassword("unknown-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.FAILED);
        replay(userService);

        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "unknown-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        tester.assertLabel("form:message", "The username or password you entered is incorrect or user is locked.");

        verify(userService);
    }

    @Test
    public void testTempPassword() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "expired-password", "test-subsystem"))
                .andReturn(UserLoginStatus.TEMP_PASSWORD);
        replay(userService);

        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "expired-password");
        form.submit();
        tester.assertRenderedPage(SSOChangePasswordPage.class);

        verify(userService);
    }
}
