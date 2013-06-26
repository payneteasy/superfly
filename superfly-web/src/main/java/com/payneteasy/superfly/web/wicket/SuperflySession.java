package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.model.StickyFilters;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginData;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

public class SuperflySession extends WebSession {
	
	private StickyFilters stickyFilters = new StickyFilters();
    private SSOLoginData ssoLoginData;

	public SuperflySession(Request request) {
		super(request);
	}

	public StickyFilters getStickyFilters() {
		return stickyFilters;
	}

    public SSOLoginData getSsoLoginData() {
        return ssoLoginData;
    }

    public void setSsoLoginData(SSOLoginData ssoLoginData) {
        this.ssoLoginData = ssoLoginData;
    }
}
