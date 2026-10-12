package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.web.wicket.page.login.LoginPasswordStepPage;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;
import org.apache.wicket.markup.html.WebPage;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpringSecurityAuthorizationStrategyTest {

    private final SpringSecurityAuthorizationStrategy strategy = new SpringSecurityAuthorizationStrategy();

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void securedPageIsDeniedToAnonymous() {
        anonymous();

        assertFalse(strategy.isInstantiationAuthorized(AdminPage.class));
        assertFalse(strategy.isInstantiationAuthorized(AdminSubPage.class));
        assertFalse(strategy.isInstantiationAuthorized(ListUsersPage.class));
    }

    @Test
    public void securedPageIsAllowedWithRole() {
        authenticated("ROLE_ADMIN");

        assertTrue(strategy.isInstantiationAuthorized(AdminPage.class));
        assertTrue(strategy.isInstantiationAuthorized(AdminSubPage.class));
        assertTrue(strategy.isInstantiationAuthorized(ListUsersPage.class));
    }

    @Test
    public void securedPageIsDeniedWithoutRequiredRole() {
        authenticated("ROLE_ACTION_TEMP_PASSWORD");

        assertFalse(strategy.isInstantiationAuthorized(ListUsersPage.class));
        assertTrue(strategy.isInstantiationAuthorized(ChangePasswordPage.class));
    }

    @Test
    public void pageWithoutSecuredIsAllowed() {
        anonymous();

        assertTrue(strategy.isInstantiationAuthorized(PublicPage.class));
        assertTrue(strategy.isInstantiationAuthorized(LoginPasswordStepPage.class));
    }

    private static void anonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    }

    private static void authenticated(String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", role));
    }

    @Secured("ROLE_ADMIN")
    private static class AdminPage extends WebPage {
    }

    private static class AdminSubPage extends AdminPage {
    }

    private static class PublicPage extends WebPage {
    }
}
