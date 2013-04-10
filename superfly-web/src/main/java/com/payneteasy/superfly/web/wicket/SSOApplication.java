package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginPage;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLogoutPage;
import org.apache.wicket.Page;

public class SSOApplication extends BaseApplication {

	@Override
	protected void customInit() {
        // SSO (i.e., real single sign-on) login
        mountBookmarkablePageWithParameters("/login", SSOLoginPage.class);
        // single sign-out
        mountBookmarkablePageWithParameters("/logout", SSOLogoutPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return SSOLoginPage.class;
	}

}
