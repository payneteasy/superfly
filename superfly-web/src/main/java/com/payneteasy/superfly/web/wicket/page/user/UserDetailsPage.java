package com.payneteasy.superfly.web.wicket.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class UserDetailsPage extends BasePage {
	@SpringBean
	private UserService userService;
	@SpringBean
	private SubsystemService subsystemService;

	public UserDetailsPage(PageParameters params) {
		super(params);
		final long userId = params.getAsLong("userId");
		UIUser thisuser = userService.getUser(userId);
		add(new Label("user-name", thisuser.getUsername()));
		UIUserWithRolesAndActions user = userService.getUserRoleActions(userId,
				null, null, null);
		final List<UIRoleWithActions> roleWithAction = user.getRoles();
		final SortRoleOfSubsystem sort = new SortRoleOfSubsystem();
		sort.setRoleWithAction(roleWithAction);
		
		ListView<String> subRolesList = new ListView<String>("sub-list",
				sort.getSubsystemsName()) {

			@Override
			protected void populateItem(ListItem<String> item) {
				final String rfc = item.getModelObject();
				item.add(new Label("sub-name", rfc.toString()));
				final PageParameters actionsParameters = new PageParameters();
				actionsParameters.add("userId", String.valueOf(userId));
				final UISubsystem subsystem = subsystemService
						.getSubsystemByName(rfc);
				actionsParameters.add("subId", String
						.valueOf(subsystem.getId()));
				item.add(new BookmarkablePageLink("add-role",
						ChangeUserRolesPage.class, actionsParameters));
				
				List<UIRoleWithActions> roles = sort.getRoles(rfc);
				item.add(new ListView<UIRoleWithActions>("role-list",
						roles) {

					@Override
					protected void populateItem(ListItem<UIRoleWithActions> it) {
						final UIRoleWithActions role = it.getModelObject();
						PageParameters params = new PageParameters();
						params.add("userId", String.valueOf(userId));
						params.add("subId", String.valueOf(subsystem.getId()));
						params.add("roleId", String.valueOf(role.getId()));
						BookmarkablePageLink<ChangeUserActionsPage> userAction = new BookmarkablePageLink<ChangeUserActionsPage>(
								"user-action", ChangeUserActionsPage.class,params);
						it.add(userAction);
						userAction.add(new Label("role-name", role.getName()));
						it.add(new Link("delete-role"){

							@Override
							public void onClick() {
								List<Long> rolesId = new ArrayList<Long>();
								rolesId.add(role.getId());
								userService.changeUserRoles(userId, null, rolesId, null);
								PageParameters parameters =new PageParameters();
								parameters.add("userId", String.valueOf(userId));
								setResponsePage(UserDetailsPage.class, parameters);
							}
							
						});
					}

				});

			}

		};
		add(subRolesList);
		
		PageParameters param = new PageParameters();
		param.add("userId",String.valueOf(userId));
		add(new BookmarkablePageLink<AddSubsystemWithRole>("add-sub",
				AddSubsystemWithRole.class,param));
	}

	@Override
	protected String getTitle() {
		return "User details";
	}

}
