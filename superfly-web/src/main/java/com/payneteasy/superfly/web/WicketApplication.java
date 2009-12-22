package com.payneteasy.superfly.web;

import org.apache.wicket.protocol.http.WebApplication;

import com.payneteasy.superfly.web.page.HomePage;

public class WicketApplication extends WebApplication{

	@Override
	public Class getHomePage() {
		// TODO Auto-generated method stub
		return HomePage.class;
	}

}
