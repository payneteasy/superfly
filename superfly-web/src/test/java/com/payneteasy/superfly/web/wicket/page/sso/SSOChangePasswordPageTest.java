package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * @author rpuch
 */
public class SSOChangePasswordPageTest extends AbstractPageTest {
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;

    @Before
    public void setUp() {
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

//    public void testNoLoginData() {
//        tester.startPage(new SSOChangePasswordPage("test-user"));
//        tester.assertRenderedPage(SSOLoginErrorPage.class);
//        tester.assertLabel("message", "No login data found");
//    }

    @Test
    public void testSuccess() throws PolicyValidationException {
        userService.validatePassword("user", "password");
        expectLastCall();
        userService.changeTempPassword("user", "password");
        expectLastCall();
        expect(sessionService.createSSOSession("user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));
        replay(userService, sessionService, subsystemService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(new SSOChangePasswordPage("user"));
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "password");
        form.setValue("password2", "password");
        form.submit();
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        verify(userService, sessionService, subsystemService);
    }

    @Test
    public void testMismatchingPassword() throws PolicyValidationException {
        userService.validatePassword("user", "password");
        replay(userService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(new SSOChangePasswordPage("user"));
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        FormTester form;

        // check password comparison logic
        form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "password");
        form.setValue("password2", "another-password");
        form.submit();
        // still on the same page
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        tester.assertLabel("change-password-panel:feedback:feedbackul:messages:0:message", "password and password2 must be equal.");
    }

    @Test
    public void testInvalidPassword() throws PolicyValidationException {
        // password validation logic
        userService.validatePassword("user", "invalid-password");
        expectLastCall().andThrow(new PolicyValidationException("P003"));
        replay(userService);

        tester.getWicketSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(new SSOChangePasswordPage("user"));
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "invalid-password");
        form.setValue("password2", "invalid-password");
        form.submit();
        // still on the same page
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        tester.assertLabel("change-password-panel:feedback:feedbackul:messages:0:message", "Password was already used");

        verify(userService);
    }
}
