package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import com.payneteasy.superfly.web.wicket.page.login.LoginErrorPage;
import org.apache.wicket.PageParameters;
import org.easymock.EasyMock;

import javax.servlet.http.Cookie;
import java.util.HashMap;

/**
 * @author rpuch
 */
public class SSOLoginPageTest extends AbstractPageTest {
    private SessionService sessionService;
    private SubsystemService subsystemService;

    public void setUp() {
        super.setUp();
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (SessionService.class == type) {
            return sessionService;
        } else if (SubsystemService.class == type) {
            return subsystemService;
        }
        return super.getBean(type);
    }

    public void testNoSSOCookie() {
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
    }

    public void testWithSSOCookieAndNoValidSession() {
        EasyMock.expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(null);

        EasyMock.replay(sessionService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);

        EasyMock.verify(sessionService);
    }

    public void testWithSSOCookieAndValidSessionAndCanNotLogin() {
        EasyMock.expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        EasyMock.expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(null);

        EasyMock.replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(LoginErrorPage.class);
        tester.assertLabel("message", "Can&#039;t login to test-subsystem");

        EasyMock.verify(sessionService, subsystemService);
    }

    public void testWithSSOCookieAndValidSessionAndCanLogin() {
        EasyMock.expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        EasyMock.expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));

        EasyMock.replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");

        EasyMock.verify(sessionService, subsystemService);
    }

    public void testWithSSOCookieAndValidSessionAndCanLoginShortenedUrl() {
        EasyMock.expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        EasyMock.expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));

        EasyMock.replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "http://some.domain/target");
        }}));
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");

        EasyMock.verify(sessionService, subsystemService);
    }
}
