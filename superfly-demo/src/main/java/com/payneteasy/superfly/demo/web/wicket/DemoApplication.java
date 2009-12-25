package com.payneteasy.superfly.demo.web.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.demo.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.demo.web.wicket.page.HomePage;

public class DemoApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        getSecuritySettings().setAuthorizationStrategy(new SpringSecurityAuthorizationStrategy());
	}

	@Override
	public Class getHomePage() {
		return HomePage.class;
	}

}
