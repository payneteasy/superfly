package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.security.SpringSecurityAuthorizationStrategy;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public abstract class BaseApplication extends WebApplication {

	@Override
	protected final void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        customInit();
	}

    protected abstract void customInit();

	@Override
	public Session newSession(Request request, Response response) {
		return new SuperflySession(request);
	}

}
