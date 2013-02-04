package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;

import static org.easymock.EasyMock.*;

/**
 * @author rpuch
 */
public class SSOLoginHOTPPageTest extends AbstractPageTest {
    private InternalSSOService internalSSOService;
    private SessionService sessionService;
    private SubsystemService subsystemService;

    public void setUp() {
        super.setUp();
        internalSSOService = EasyMock.createStrictMock(InternalSSOService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == InternalSSOService.class) {
            return internalSSOService;
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
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }

    public void testSuccess() {
        expect(internalSSOService.authenticateHOTP("test-subsystem", "known-user", "111111"))
                .andReturn(true);
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));
        replay(internalSSOService, sessionService, subsystemService);

        SSOLoginData loginData = createLoginData();
        tester.getWicketSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", "111111");
        form.submit();
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        verify(internalSSOService, sessionService, subsystemService);
    }

    private SSOLoginData createLoginData() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setUsername("known-user");
        return loginData;
    }

    public void testFailure() {
        expect(internalSSOService.authenticateHOTP("test-subsystem", "known-user", "222222"))
                        .andReturn(false);
        replay(internalSSOService);

        tester.getWicketSession().setSsoLoginData(createLoginData());
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", "222222");
        form.submit();
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        tester.assertLabel("form:message", "One-time password value did not match.");

        verify(internalSSOService);
    }
}
