package com.payneteasy.superfly.web.wicket;

import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.web.wicket.page.action.CopyActionPropertiesPage;
import com.payneteasy.superfly.web.wicket.page.action.ListActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.ChangeGroupActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.CloneGroupPage;
import com.payneteasy.superfly.web.wicket.page.group.ListGroupsPage;
import com.payneteasy.superfly.web.wicket.page.group.ViewGroupPage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupActionsPage;
import com.payneteasy.superfly.web.wicket.page.group.wizard.GroupPropertiesPage;
import com.payneteasy.superfly.web.wicket.page.login.LoginHOTPStepPage;
import com.payneteasy.superfly.web.wicket.page.login.LoginPageWithoutHOTP;
import com.payneteasy.superfly.web.wicket.page.login.LoginPasswordStepPage;
import com.payneteasy.superfly.web.wicket.page.role.*;
import com.payneteasy.superfly.web.wicket.page.session.ListSessionsPage;
import com.payneteasy.superfly.web.wicket.page.smtp_server.CreateSmtpServerPage;
import com.payneteasy.superfly.web.wicket.page.smtp_server.ListSmtpServersPage;
import com.payneteasy.superfly.web.wicket.page.smtp_server.UpdateSmtpServerPage;
import com.payneteasy.superfly.web.wicket.page.smtp_server.ViewSmtpServerPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.AddSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.EditSubsystemPage;
import com.payneteasy.superfly.web.wicket.page.subsystem.ListSubsystemsPage;
import com.payneteasy.superfly.web.wicket.page.user.*;
import com.payneteasy.superfly.wicket.InterceptionDecisions;
import com.payneteasy.superfly.wicket.PageInterceptingRequestMapper;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;

public class SuperflyApplication extends BaseApplication {

	@Override
	protected void customInit() {
        getSecuritySettings().setAuthorizationStrategy(new SpringSecurityAuthorizationStrategy());
        CryptoMapper requestMapper = new CryptoMapper(getRootRequestMapper(), this);
        setRootRequestMapper(wrapWithInterceptingMapper(requestMapper));

        mountBookmarkablePageWithPath("/loginbase", LoginPageWithoutHOTP.class);
        mountBookmarkablePageWithPath("/login", LoginPasswordStepPage.class);
        mountBookmarkablePageWithPath("/login-step2", LoginHOTPStepPage.class);

        mountBookmarkablePageWithPath("/actions", ListActionsPage.class);
        mountBookmarkablePageWithPath("/actions/copyAction", CopyActionPropertiesPage.class);
        
        mountBookmarkablePageWithPath("/groups", ListGroupsPage.class);
        mountBookmarkablePageWithPath("/groups/cloneGroup", CloneGroupPage.class);
        mountBookmarkablePageWithPath("/groups/changeGroupActions", ChangeGroupActionsPage.class);
        mountBookmarkablePageWithPath("/groups/view", ViewGroupPage.class);
        mountBookmarkablePageWithPath("/groups/add/addActions", GroupActionsPage.class);
        mountBookmarkablePageWithPath("/groups/add", GroupPropertiesPage.class);
        
        mountBookmarkablePageWithPath("/roles", ListRolesPage.class);
        mountBookmarkablePageWithPath("/roles/view", ViewRolePage.class);
        mountBookmarkablePageWithPath("/roles/update", EditRolePage.class);
        mountBookmarkablePageWithPath("/roles/add", AddRolePage.class);
        mountBookmarkablePageWithPath("/roles/add/addActions", AddRoleActionsPage.class);
        mountBookmarkablePageWithPath("/roles/add/addGroups", AddRoleGroupsPage.class);
        mountBookmarkablePageWithPath("/roles/changeActions", ChangeRoleActionsPage.class);
        mountBookmarkablePageWithPath("/roles/changeGroups", ChangeRoleGroupsPage.class);
        
        mountBookmarkablePageWithPath("/subsystems", ListSubsystemsPage.class);
        mountBookmarkablePageWithPath("/subsystems/add", AddSubsystemPage.class);
        mountBookmarkablePageWithPath("/subsystems/update", EditSubsystemPage.class);
        
        
        mountBookmarkablePageWithPath("/users", ListUsersPage.class);
        mountBookmarkablePageWithPath("/users/create", CreateUserPage.class);
        mountBookmarkablePageWithPath("/users/update", EditUserPage.class);
        mountBookmarkablePageWithPath("/users/clone", CloneUserPage.class);
        mountBookmarkablePageWithPath("/users/changeRoles", ChangeUserRolesPage.class);
        mountBookmarkablePageWithPath("/users/changeActions", ChangeUserGrantActionsPage.class);
        mountBookmarkablePageWithPath("/users/view", UserDetailsPage.class);
        mountBookmarkablePageWithPath("/users/addSubsystemWithRole", AppendSubsystemWithRolePage.class);
        
        mountBookmarkablePageWithPath("/sessions", ListSessionsPage.class);
        mountBookmarkablePageWithPath("/changepassword", ChangePasswordPage.class);

        mountBookmarkablePageWithPath("smtp-servers", ListSmtpServersPage.class);
        mountBookmarkablePageWithPath("smtp-servers/create", CreateSmtpServerPage.class);
        mountBookmarkablePageWithPath("smtp-servers/update", UpdateSmtpServerPage.class);
        mountBookmarkablePageWithPath("smtp-servers/view", ViewSmtpServerPage.class);
	}

    protected InterceptionDecisions createInterceptionDecisions() {
        return new InterceptionDecisions() {
            @Override
            public boolean shouldIntercept(Request requestCycle) {
                return SecurityUtils.isTempPassword();
            }
        };
    }

    @Override
    protected IRequestMapper wrapWithInterceptingMapper(IRequestMapper mapper) {
        return new PageInterceptingRequestMapper(mapper,
                createInterceptionDecisions(), ChangePasswordPage.class);
    }

    @Override
	public Class<? extends Page> getHomePage() {
		return ListUsersPage.class;
	}

}
