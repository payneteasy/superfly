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
import jakarta.servlet.http.Cookie;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SSOSESSIONID must be Secure, HttpOnly and SameSite=Lax even on a plain-http request
 * (TLS is terminated by a proxy in front of Jetty).
 */
public class SSOSessionCookieFlagsTest extends AbstractPageTest {
    private final UserService userService = EasyMock.createNiceMock(UserService.class);
    private final SessionService sessionService = EasyMock.createNiceMock(SessionService.class);
    private final SubsystemService subsystemService = EasyMock.createNiceMock(SubsystemService.class);
    private final SettingsService settingsService = EasyMock.createNiceMock(SettingsService.class);
    private final CsrfValidator csrfValidator = EasyMock.createNiceMock(CsrfValidator.class);

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

    @Test
    public void ssoSessionCookieHasSecurityFlags() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        expect(subsystemService.getSubsystemByName("test-subsystem")).andReturn(subsystem).anyTimes();
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        UserForDescription userForDescription = EasyMock.createNiceMock(UserForDescription.class);
        expect(userForDescription.getOtpType()).andReturn(OTPType.NONE).anyTimes();
        expect(userForDescription.isOtpOptional()).andReturn(Boolean.FALSE).anyTimes();
        expect(userService.getUserForDescription("known-user")).andReturn(userForDescription).anyTimes();
        expect(sessionService.createSSOSession("known-user")).andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(userForDescription, userService, sessionService, subsystemService, settingsService, csrfValidator);

        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(SSOLoginPasswordPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        form.submit();

        Cookie cookie = tester.getLastResponse().getCookies().stream()
                .filter(c -> SSOUtils.SSO_SESSION_ID_COOKIE_NAME.equals(c.getName()))
                .findFirst().orElse(null);
        assertNotNull(cookie);
        assertTrue("Secure", cookie.getSecure());
        assertTrue("HttpOnly", cookie.isHttpOnly());
        assertEquals("Lax", cookie.getAttribute("SameSite"));
    }
}
