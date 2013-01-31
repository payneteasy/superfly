package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;

/**
 * @author rpuch
 */
public class SSOLoginPasswordPageTest extends AbstractPageTest {
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;

    public void setUp() {
        super.setUp();
        userService = EasyMock.createStrictMock(UserService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
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
        return super.getBean(type);
    }

    public void testNoLoginData() {
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }

    public void testSuccess() {
        EasyMock.expect(userService.getUserLoginStatus("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        EasyMock.expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        EasyMock.expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));
        EasyMock.replay(userService, sessionService, subsystemService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        EasyMock.verify(userService, sessionService, subsystemService);
    }

    public void testFailure() {
        EasyMock.expect(userService.getUserLoginStatus("unknown-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.FAILED);
        EasyMock.replay(userService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "unknown-user");
        form.setValue("password", "password");
        form.submit();
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        tester.assertLabel("form:message", "The username or password you entered is incorrect or user is locked.");

        EasyMock.verify(userService);
    }
}
