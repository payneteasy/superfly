package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.web.wicket.page.HomePage;
import com.payneteasy.superfly.web.wicket.page.action.ActionsList;
import com.payneteasy.superfly.web.wicket.page.group.GroupListPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.SubsystemListPage;

public class WicketApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        mountBookmarkablePage("/actions", ActionsList.class);
        mountBookmarkablePage("/groups", GroupListPage.class);
        mountBookmarkablePage("/subsystems", SubsystemListPage.class);
	}

	@Override
	public Class getHomePage() {
		return HomePage.class;
	}

}
