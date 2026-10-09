package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author rpuch
 */
public class SSOLoginPageTest extends AbstractPageTest {
    private static final String STATE = "Ab0_-cdefghijklmnopqrstuvwxyz0123456789ABCDE";

    private SessionService sessionService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        csrfValidator = EasyMock.createStrictMock(CsrfValidator.class);

        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (SessionService.class == type) {
            return sessionService;
        } else if (SubsystemService.class == type) {
            return subsystemService;
        } else if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void testNoSSOCookie() {
        UISubsystem subsystem = createTestSubsystem();
        expect(subsystemService.getSubsystemByName("test-subsystem"))
                .andReturn(subsystem).anyTimes();
        replay(subsystemService, csrfValidator);
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        assertEquals("the subsystem", tester.getSession().getSsoLoginData().getSubsystemTitle());
        assertEquals("subsystem-url", tester.getSession().getSsoLoginData().getSubsystemUrl());

        verify(subsystemService, csrfValidator);
    }

    private UISubsystem createTestSubsystem() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("the subsystem");
        subsystem.setSubsystemUrl("subsystem-url");
        return subsystem;
    }

    @Test
    public void testWithSSOCookieAndNoValidSession() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(null);
        expect(subsystemService.getSubsystemByName("test-subsystem"))
                        .andReturn(createTestSubsystem()).anyTimes();

        replay(sessionService, subsystemService, csrfValidator);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);

        verify(sessionService, subsystemService, csrfValidator);
    }

    @Test
    public void testWithSSOCookieAndValidSessionAndCanNotLogin() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(null);

        replay(sessionService, subsystemService);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "Can&#039;t login to test-subsystem");

        verify(sessionService, subsystemService);
    }

    @Test
    public void testWithSSOCookieAndValidSessionAndCanLogin() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));

        replay(sessionService, subsystemService);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
        }}));
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");

        verify(sessionService, subsystemService);
    }

    @Test
    public void testWithSSOCookieAndValidSessionAndCanLoginShortenedUrl() {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));

        replay(sessionService, subsystemService);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "http://some.domain/target");
        }}));
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");

        verify(sessionService, subsystemService);
    }

    @Test
    public void testStateEchoedWithSSOCookie() {
        startWithValidSSOCookie(STATE);
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget&state=" + STATE);
        assertNull("state is one-time", tester.getSession().getSsoLoginData().getState());
        verify(sessionService, subsystemService);
    }

    @Test
    public void testTooShortStateNotEchoed() {
        startWithValidSSOCookie("abcdefghijklmno");
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        verify(sessionService, subsystemService);
    }

    @Test
    public void testTooLongStateNotEchoed() {
        startWithValidSSOCookie("a".repeat(129));
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        verify(sessionService, subsystemService);
    }

    @Test
    public void testStateWithForeignCharactersNotEchoed() {
        startWithValidSSOCookie("abcdefghijklmnop&x=<y>");
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        verify(sessionService, subsystemService);
    }

    @Test
    public void testLongestStateEchoed() {
        String state = "a".repeat(128);
        startWithValidSSOCookie(state);
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget&state=" + state);
        verify(sessionService, subsystemService);
    }

    @Test
    public void testStateKeptForPasswordStep() {
        expect(subsystemService.getSubsystemByName("test-subsystem")).andReturn(createTestSubsystem()).anyTimes();
        replay(subsystemService, csrfValidator);
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
            put("state", STATE);
        }}));
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
        assertEquals(STATE, tester.getSession().getSsoLoginData().getState());
    }

    private void startWithValidSSOCookie(String state) {
        expect(sessionService.getValidSSOSession("super-session-id"))
                .andReturn(new SSOSession(1, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(sessionService, subsystemService);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "super-session-id"));
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test-subsystem");
            put("targetUrl", "/target");
            put("state", state);
        }}));
    }

    @Test
    public void testNoParams() {
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", "test");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No targetUrl parameter specified");

        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("targetUrl", "/target");
        }}));
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No subsystemIdentifier parameter specified");
    }
}
