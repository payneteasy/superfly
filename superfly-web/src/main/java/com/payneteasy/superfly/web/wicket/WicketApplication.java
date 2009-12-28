package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.web.wicket.page.HomePage;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.user.AddUserPage;
import com.payneteasy.superfly.web.wicket.page.user.CloneUserPage;
import com.payneteasy.superfly.web.wicket.page.user.EditUserPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;

public class WicketApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        mountBookmarkablePage("/actions", ListActionsPage.class);
        mountBookmarkablePage("/groups", ListGroupPage.class);
        mountBookmarkablePage("/subsystems", ListSubsystemPage.class);
        
        mountBookmarkablePage("/users", ListUsersPage.class);
        mountBookmarkablePage("/users/create", AddUserPage.class);
        mountBookmarkablePage("/users/update", EditUserPage.class);
        mountBookmarkablePage("/users/clone", CloneUserPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
