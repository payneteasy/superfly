package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.wicket.page.user.*;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import com.payneteasy.superfly.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.web.wicket.page.HomePage;
import com.payneteasy.superfly.web.wicket.page.action.CopyActionPropertiesPage;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ChangeGroupActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.CloneGroupPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.group.ViewGroupPage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupPropertiesPage;
import com.payneteasy.superfly.web.wicket.page.role.AddRoleActionsPage;
import com.payneteasy.superfly.web.wicket.page.role.AddRoleGroupsPage;
import com.payneteasy.superfly.web.wicket.page.role.AddRolePage;
import com.payneteasy.superfly.web.wicket.page.role.ChangeRoleActionsPage;
import com.payneteasy.superfly.web.wicket.page.role.ChangeRoleGroupsPage;
import com.payneteasy.superfly.web.wicket.page.role.EditRolePage;
import com.payneteasy.superfly.web.wicket.page.role.ListRolesPage;
import com.payneteasy.superfly.web.wicket.page.role.ViewRolePage;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.AddSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.EditSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemsPage;

public class SuperflyApplication extends WebApplication{

	@Override
	protected void init() {
		getResourceSettings().addResourceFolder("src/main/java");
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getSecuritySettings().setAuthorizationStrategy(new SpringSecurityAuthorizationStrategy());
        getDebugSettings().setOutputMarkupContainerClassName(false);
        
        mountBookmarkablePage("/actions", ListActionsPage.class);
        mountBookmarkablePage("/actions/copyAction", CopyActionPropertiesPage.class);
        
        mountBookmarkablePage("/groups", ListGroupsPage.class);
        mountBookmarkablePage("/groups/cloneGroup", CloneGroupPage.class);
        mountBookmarkablePage("/groups/changeGroupActions", ChangeGroupActionsPage.class);
        mountBookmarkablePage("/groups/view", ViewGroupPage.class);
        mountBookmarkablePage("/groups/add/addActions", GroupActionsPage.class);
        mountBookmarkablePage("/groups/add", GroupPropertiesPage.class);
        
        mountBookmarkablePage("/roles", ListRolesPage.class);
        mountBookmarkablePage("/roles/view", ViewRolePage.class);
        mountBookmarkablePage("/roles/update", EditRolePage.class);
        mountBookmarkablePage("/roles/add", AddRolePage.class);
        mountBookmarkablePage("/roles/add/addActions", AddRoleActionsPage.class);
        mountBookmarkablePage("/roles/add/addGroups", AddRoleGroupsPage.class);
        mountBookmarkablePage("/roles/changeActions", ChangeRoleActionsPage.class);
        mountBookmarkablePage("/roles/changeGroups", ChangeRoleGroupsPage.class);
        
        mountBookmarkablePage("/subsystems", ListSubsystemsPage.class);
        mountBookmarkablePage("/subsystems/add", AddSubsystemPage.class);
        mountBookmarkablePage("/subsystems/update", EditSubsystemPage.class);
        
        
        mountBookmarkablePage("/users", ListUsersPage.class);
        mountBookmarkablePage("/users/create", CreateUserPage.class);
        mountBookmarkablePage("/users/update", EditUserPage.class);
        mountBookmarkablePage("/users/clone", CloneUserPage.class);
        mountBookmarkablePage("/users/changeRoles", ChangeUserRolesPage.class);
        mountBookmarkablePage("/users/changeActions", ChangeUserActionsPage.class);
        mountBookmarkablePage("/users/view", UserDetailsPage.class);
        mountBookmarkablePage("/users/addSubsystemWithRole", AddSubsystemWithRolePage.class);
        
        mountBookmarkablePage("/sessions", ListSessionsPage.class);
        mountBookmarkablePage("/changepassword", ChangePasswordPage.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

	@Override
	public Session newSession(Request request, Response response) {
		return new SuperflySession(request);
	}
    
}
