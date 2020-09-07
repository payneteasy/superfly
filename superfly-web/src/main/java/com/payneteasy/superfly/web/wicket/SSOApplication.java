package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginPage;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLogoutPage;
import org.apache.wicket.Page;
import org.apache.wicket.settings.RequestCycleSettings;

public class SSOApplication extends BaseApplication {

    @Override
    protected void customInit() {
        // SSO (i.e., real single sign-on) login
        mountBookmarkablePageWithParameters("/login", SSOLoginPage.class);
        // single sign-out
        mountBookmarkablePageWithParameters("/logout", SSOLogoutPage.class);

        getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return SSOLoginPage.class;
    }

}
