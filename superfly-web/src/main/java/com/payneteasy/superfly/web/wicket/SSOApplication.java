package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.*;
import org.apache.wicket.Page;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.settings.RequestCycleSettings;

import jakarta.servlet.http.HttpServletResponse;

public class SSOApplication extends BaseApplication {

    @Override
    protected void customInit() {
        getSecuritySettings().setAuthorizationStrategy(new SSOAuthorizationStrategy());
        // thrown from the Component constructor: the denied page is neither constructed nor rendered
        getSecuritySettings().setUnauthorizedComponentInstantiationListener(component -> {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_FORBIDDEN);
        });
        // SSO (i.e., real single sign-on) login
        mountBookmarkablePageWithParameters("/login", SSOLoginPage.class);
        // single sign-out
        mountBookmarkablePageWithParameters("/logout", SSOLogoutPage.class);
        mountBookmarkablePageWithParameters("/login-otp", SSOLoginHOTPPage.class);
        mountBookmarkablePageWithParameters("/error-page", SSOLoginErrorPage.class);
        mountBookmarkablePageWithParameters("/ga-setup", SSOSetupGoogleAuthPage.class);

        getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return SSOLoginPage.class;
    }

}
