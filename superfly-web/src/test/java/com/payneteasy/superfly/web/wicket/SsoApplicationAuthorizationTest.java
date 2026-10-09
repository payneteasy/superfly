package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SmtpServerService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginPasswordPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.EditSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.user.CreateUserPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The public SSO application (/sso/*) must not instantiate admin pages requested by class name
 * (security audit 2026-10-08, C1).
 */
public class SsoApplicationAuthorizationTest {

    private WicketTester tester;

    @Before
    public void setUp() {
        SubsystemService subsystemService = niceMock(SubsystemService.class);
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("billing");
        subsystem.setTitle("Billing");
        subsystem.setSubsystemUrl("https://billing.example.com");
        expect(subsystemService.getSubsystemByName(anyString())).andStubReturn(subsystem);
        replay(subsystemService);

        SSOApplication app = new SSOApplication();
        MockServletContext servletContext = new MockServletContext(app, null);
        StaticWebApplicationContext ctx = new StaticWebApplicationContext();
        ctx.setServletContext(servletContext);
        ctx.getBeanFactory().registerSingleton("subsystemService", subsystemService);
        ctx.getBeanFactory().registerSingleton("settingsService", mock(SettingsService.class));
        ctx.getBeanFactory().registerSingleton("sessionService", mock(SessionService.class));
        ctx.getBeanFactory().registerSingleton("userService", mock(UserService.class));
        ctx.getBeanFactory().registerSingleton("roleService", mock(RoleService.class));
        ctx.getBeanFactory().registerSingleton("smtpServerService", mock(SmtpServerService.class));
        ctx.getBeanFactory().registerSingleton("hotpProvider", mock(HOTPProvider.class));
        ctx.getBeanFactory().registerSingleton("csrfValidator", mock(CsrfValidator.class));
        ctx.refresh();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ctx);

        // what Spring Security's AnonymousAuthenticationFilter gives an unauthenticated client
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        tester = new WicketTester(app, servletContext);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
        if (tester != null) {
            tester.destroy();
        }
    }

    @Test
    public void listSessionsPageIsForbidden() {
        assertForbidden(ListSessionsPage.class);
    }

    @Test
    public void listUsersPageIsForbidden() {
        assertForbidden(ListUsersPage.class);
    }

    @Test
    public void createUserPageIsForbidden() {
        assertForbidden(CreateUserPage.class);
    }

    @Test
    public void editSubsystemPageIsForbidden() {
        assertForbidden(EditSubsystemPage.class);
    }

    @Test
    public void ssoLoginStillWorks() {
        tester.executeUrl("login?subsystemIdentifier=billing&targetUrl=/");

        assertEquals(200, tester.getLastResponse().getStatus());
        tester.assertRenderedPage(SSOLoginPasswordPage.class);
    }

    private void assertForbidden(Class<? extends Page> pageClass) {
        tester.executeUrl("wicket/bookmarkable/" + pageClass.getName());

        assertEquals(403, tester.getLastResponse().getStatus());
        assertNull(tester.getLastRenderedPage());
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
