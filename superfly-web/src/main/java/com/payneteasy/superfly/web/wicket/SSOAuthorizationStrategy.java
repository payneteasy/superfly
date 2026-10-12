package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginPage;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;

import java.util.Set;

/**
 * The SSO application is public (/sso/** is permitAll) but shares the classpath with the admin UI,
 * so any page class could be instantiated through it by name. Only SSO pages and Wicket's own
 * error/service pages may be instantiated here.
 */
public class SSOAuthorizationStrategy implements IAuthorizationStrategy {

    private static final Set<String> ALLOWED_PAGE_PACKAGES = Set.of(
            SSOLoginPage.class.getPackageName(),
            // PageExpiredErrorPage, AccessDeniedPage, InternalErrorPage, ExceptionErrorPage, BrowserInfoPage
            AccessDeniedPage.class.getPackageName()
    );

    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        if (!Page.class.isAssignableFrom(componentClass)) {
            return true;
        }
        return ALLOWED_PAGE_PACKAGES.contains(componentClass.getPackageName());
    }

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        return true;
    }

    @Override
    public boolean isResourceAuthorized(IResource resource, PageParameters parameters) {
        return true;
    }
}
