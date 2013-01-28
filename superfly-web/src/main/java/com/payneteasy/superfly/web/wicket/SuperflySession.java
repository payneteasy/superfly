package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.login.SSOLoginData;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

import com.payneteasy.superfly.web.wicket.model.StickyFilters;

public class SuperflySession extends WebSession {
	
	private StickyFilters stickyFilters = new StickyFilters();
    private SSOLoginData ssoLoginData = new SSOLoginData();

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
