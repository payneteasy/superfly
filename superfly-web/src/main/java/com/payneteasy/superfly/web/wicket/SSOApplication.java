package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginPage;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLogoutPage;
import com.payneteasy.superfly.wicket.SessionStoreUrlWebRequestCodingStrategy;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.IRequestCycleProcessor;

public class SSOApplication extends BaseApplication {

	@Override
	protected void customInit() {
        // SSO (i.e., real single sign-on) login
        mountBookmarkablePage("/login", SSOLoginPage.class);
        // single sign-out
        mountBookmarkablePage("/logout", SSOLogoutPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		// TODO: show some error text or redirect somewhere... possibly, to /
        return null;
	}

	@Override
	protected IRequestCycleProcessor newRequestCycleProcessor() {
		return new WebRequestCycleProcessor() {
            @Override
            public IRequestCodingStrategy newRequestCodingStrategy() {
                return new SessionStoreUrlWebRequestCodingStrategy(
                        new WebRequestCodingStrategy());
            }
        };
	}
    
}
