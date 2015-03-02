package com.payneteasy.superfly.demo.web.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.demo.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.demo.web.wicket.page.HomePage;
import org.apache.wicket.util.file.Path;

public class DemoApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().getResourceFinders().add(0, new Path("src/main/java"));
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        getSecuritySettings().setAuthorizationStrategy(new SpringSecurityAuthorizationStrategy());
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
