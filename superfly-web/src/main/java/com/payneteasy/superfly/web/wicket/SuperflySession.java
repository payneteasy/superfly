package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebSession;

import com.payneteasy.superfly.web.wicket.model.StickyFilters;

public class SuperflySession extends WebSession {
	
	private StickyFilters stickyFilters = new StickyFilters();

	public SuperflySession(Request request) {
		super(request);
	}

	public StickyFilters getStickyFilters() {
		return stickyFilters;
	}

}
