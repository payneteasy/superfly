package com.payneteasy.superfly.web.wicket.page.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
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
		List<String> subsystemsName = new ArrayList<String>();
		for (UIRoleWithActions rwa : roleWithAction) {
			if (!subsystemsName.contains(rwa.getSubsystemName())) {
				subsystemsName.add(rwa.getSubsystemName());
			}
		}
		ListView<String> subRolesList = new ListView<String>(
				"sub-list", subsystemsName) {

			@Override
			protected void populateItem(ListItem<String> item) {
				final String rfc = item.getModelObject();
				item.add(new Label("sub-name", rfc.toString()));
				PageParameters actionsParameters = new PageParameters();
				actionsParameters.add("userId", String.valueOf(userId));
				UISubsystem subsystem = subsystemService.getSubsystemByName(rfc);
				actionsParameters.add("subId", String.valueOf(subsystem.getId()));
				item.add(new BookmarkablePageLink("add-role",ChangeUserRolesPage.class,actionsParameters));
				item.add(new ListView<UIRoleWithActions>("role-list",
						roleWithAction) {

					@Override
					protected void populateItem(ListItem<UIRoleWithActions> it) {
						UIRoleWithActions roles = it.getModelObject();
						String roleName = "";
						if (rfc.toString().equals(
								roles.getSubsystemName())) {
							roleName = roles.getName();
						}
						it.add(new Label("role-name", roleName));
					}

				});

			}

		};
		add(subRolesList);
		add(new BookmarkablePageLink<ListUsersPage>("cancel-link",
				ListUsersPage.class));
	}

	@Override
	protected String getTitle() {
		return "User details";
	}

}
