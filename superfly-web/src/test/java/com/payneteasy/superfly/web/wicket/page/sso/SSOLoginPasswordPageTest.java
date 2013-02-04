package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import junit.framework.Assert;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;

import static org.easymock.EasyMock.*;

/**
 * @author rpuch
 */
public class SSOLoginPasswordPageTest extends AbstractPageTest {
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private SettingsService settingsService;

    public void setUp() {
        super.setUp();
        userService = EasyMock.createStrictMock(UserService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        settingsService = EasyMock.createStrictMock(SettingsService.class);
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

    public void testNoLoginData() {
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }

    public void testSuccessNoHOTP() {
        expect(userService.getUserLoginStatus("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));
        expect(settingsService.getPolicy()).andReturn(Policy.NONE);
        expect(settingsService.isHotpDisabled()).andReturn(true).anyTimes();
        replay(userService, sessionService, subsystemService, settingsService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        verify(userService, sessionService, subsystemService, settingsService);
    }

    public void testSuccessAndHOTP() {
        expect(userService.getUserLoginStatus("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(settingsService.getPolicy()).andReturn(Policy.PCIDSS);
        expect(settingsService.isHotpDisabled()).andReturn(false);
        replay(userService, settingsService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        Assert.assertEquals("known-user", tester.getWicketSession().getSsoLoginData().getUsername());

        verify(userService, settingsService);
    }

    public void testFailure() {
        expect(userService.getUserLoginStatus("unknown-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.FAILED);
        replay(userService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
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

    public void testTempPassword() {
        expect(userService.getUserLoginStatus("known-user", "expired-password", "test-subsystem"))
                .andReturn(UserLoginStatus.TEMP_PASSWORD);
        replay(userService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
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
