package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.PageParameters;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.web.InjectorHolder;
import org.easymock.EasyMock;

import javax.servlet.http.Cookie;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * @author rpuch
 */
public class SSOLoginPageTest extends TestCase {
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private Tester tester;

    public void setUp() {
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        tester = new Tester();
        tester.getApplication().addComponentInstantiationListener(new ComponentInjector() {{
            InjectorHolder.setInjector(createInjector());
        }});
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
                .andReturn(new SSOSession(1));
        EasyMock.expect(subsystemService.getSubsystemTokenIfCanLogin(1, "test-subsystem"))
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
                .andReturn(new SSOSession(1));
        EasyMock.expect(subsystemService.getSubsystemTokenIfCanLogin(1, "test-subsystem"))
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
                .andReturn(new SSOSession(1));
        EasyMock.expect(subsystemService.getSubsystemTokenIfCanLogin(1, "test-subsystem"))
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

    private ConfigurableInjector createInjector() {
        return new ConfigurableInjector() {
            @Override
            protected IFieldValueFactory getFieldValueFactory() {
                return createFieldValueFactory();
            }
        };
    }

    private IFieldValueFactory createFieldValueFactory() {
        return new IFieldValueFactory() {
            @Override
            public Object getFieldValue(Field field, Object fieldOwner) {
                if (SessionService.class == field.getType()) {
                    return sessionService;
                } else if (SubsystemService.class == field.getType()) {
                    return subsystemService;
                }
                throw new IllegalStateException("Canno instantiate " + field.getType());
            }

            @Override
            public boolean supportsField(Field field) {
                return SessionService.class == field.getType()
                        || SubsystemService.class == field.getType();
            }
        };
    }
}
