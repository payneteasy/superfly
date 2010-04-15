package com.payneteasy.superfly.web.wicket;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.web.wicket.page.HomePage;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemsPage;
import com.payneteasy.superfly.web.wicket.page.user.ChangeUserActionsPage;
import com.payneteasy.superfly.web.wicket.page.user.ChangeUserRolesPage;
import com.payneteasy.superfly.web.wicket.page.user.CloneUserPage;
import com.payneteasy.superfly.web.wicket.page.user.CreateUserPage;
import com.payneteasy.superfly.web.wicket.page.user.EditUserPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;
import com.payneteasy.superfly.web.wicket.page.user.UserDetailsPage;

public class WicketApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getSecuritySettings().setAuthorizationStrategy(new SpringSecurityAuthorizationStrategy());
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        mountBookmarkablePage("/actions", ListActionsPage.class);
        mountBookmarkablePage("/groups", ListGroupsPage.class);
        mountBookmarkablePage("/subsystems", ListSubsystemsPage.class);
        
        mountBookmarkablePage("/users", ListUsersPage.class);
        mountBookmarkablePage("/users/create", CreateUserPage.class);
        mountBookmarkablePage("/users/update", EditUserPage.class);
        mountBookmarkablePage("/users/clone", CloneUserPage.class);
        mountBookmarkablePage("/users/changeRoles", ChangeUserRolesPage.class);
        mountBookmarkablePage("/users/changeActions", ChangeUserActionsPage.class);
        mountBookmarkablePage("/users/view", UserDetailsPage.class);
        
        mountBookmarkablePage("/sessions", ListSessionsPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
