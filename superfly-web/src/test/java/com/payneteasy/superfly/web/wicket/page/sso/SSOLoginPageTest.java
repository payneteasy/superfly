package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import junit.framework.Assert;
import org.apache.wicket.PageParameters;
import org.easymock.EasyMock;

import javax.servlet.http.Cookie;
import java.util.HashMap;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

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
        UISubsystem subsystem = createTestSubsystem();
        expect(subsystemService.getSubsystemByName("test-subsystem"))
                .andReturn(subsystem);
        replay(subsystemService);
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        Assert.assertEquals("the subsystem", tester.getWicketSession().getSsoLoginData().getSubsystemTitle());
        Assert.assertEquals("subsystem-url", tester.getWicketSession().getSsoLoginData().getSubsystemUrl());

        verify(subsystemService);
    }

    private UISubsystem createTestSubsystem() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("the subsystem");
        subsystem.setSubsystemUrl("subsystem-url");
        return subsystem;
    }

    public void testWithSSOCookieAndNoValidSession() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(null);
        expect(subsystemService.getSubsystemByName("test-subsystem"))
                        .andReturn(createTestSubsystem());

        replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);

        verify(sessionService, subsystemService);
    }

    public void testWithSSOCookieAndValidSessionAndCanNotLogin() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(null);

        replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "Can&#039;t login to test-subsystem");

        verify(sessionService, subsystemService);
    }

    public void testWithSSOCookieAndValidSessionAndCanLogin() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));

        replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");

        verify(sessionService, subsystemService);
    }

    public void testWithSSOCookieAndValidSessionAndCanLoginShortenedUrl() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://localhost/landing-url"));

        replay(sessionService, subsystemService);

        tester.getServletResponse().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "http://some.domain/target");
        }}));
        tester.assertRedirected("http://localhost/landing-url?subsystemToken=abcdef&targetUrl=/target");

        verify(sessionService, subsystemService);
    }

    public void testNoParams() {
        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No targetUrl parameter specified");

        tester.startPage(SSOLoginPage.class, new PageParameters(new HashMap<String, Object>() {{
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No subsystemIdentifier parameter specified");
    }
}
