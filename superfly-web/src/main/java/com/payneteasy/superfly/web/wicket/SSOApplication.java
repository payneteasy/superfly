package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.*;
import org.apache.wicket.Page;
import org.apache.wicket.settings.RequestCycleSettings;

public class SSOApplication extends BaseApplication {

    @Override
    protected void customInit() {
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
