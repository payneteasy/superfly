package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.web.wicket.page.HomePage;

public class WicketApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
	}

	@Override
	public Class getHomePage() {
		return HomePage.class;
	}

}
