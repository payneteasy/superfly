package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.PagingDataView;
import com.payneteasy.superfly.web.wicket.component.RoleChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.label.DateLabels;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.repeater.IndexedSortableDataProvider;

/**
 * Displays a list of users.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ListUsersPage extends BasePage {
	
	@SpringBean
	private UserService userService;
	@SpringBean
	private RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;

	public ListUsersPage() {
		super();
		
		// filters
		final UserFilters userFilters = new UserFilters();
		Form<UserFilters> filtersForm = new Form<UserFilters>("filters-form");
		add(filtersForm);
		filtersForm.add(new TextField<String>("username-filter",
				new PropertyModel<String>(userFilters, "usernamePrefix")));
		DropDownChoice<UIRoleForFilter> roleDropdown = new DropDownChoice<UIRoleForFilter>("role-filter",
				new PropertyModel<UIRoleForFilter>(userFilters, "role"),
				roleService.getRolesForFilter(), new RoleChoiceRenderer());
		roleDropdown.setNullValid(true);
		filtersForm.add(roleDropdown);
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>("subsystem-filter",
				new PropertyModel<UISubsystemForFilter>(userFilters, "subsystem"),
				subsystemService.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
		
		// data provider + sortability
		String[] fieldNames = {"userId", "username", "password", "locked",
				"loginsFailed", "lastLoginDate"};
		SortableDataProvider<UIUserForList> usersDataProvider = new IndexedSortableDataProvider<UIUserForList>(fieldNames) {
			public Iterator<? extends UIUserForList> iterator(int first,
					int count) {
				UIRoleForFilter role = userFilters.getRole();
				UISubsystemForFilter subsystem = userFilters.getSubsystem();
				return userService.getUsers(userFilters.getUsernamePrefix(),
						role == null ? null : role.getId(), null,
						subsystem == null ? null : subsystem.getId(),
						first, count, getSortFieldIndex(), isAscending()).iterator();
			}

			public int size() {
				UIRoleForFilter role = userFilters.getRole();
				UISubsystemForFilter subsystem = userFilters.getSubsystem();
				return userService.getUsersCount(userFilters.getUsernamePrefix(),
						role == null ? null : role.getId(), null,
						subsystem == null ? null : subsystem.getId());
			}

		};
		
		// data itself
		DataView<UIUserForList> usersDataView = new PagingDataView<UIUserForList>("usersList", usersDataProvider) {
			@Override
			protected void populateItem(Item<UIUserForList> item) {
				final UIUserForList user = item.getModelObject();
				PageParameters actionsParameters = new PageParameters();
				actionsParameters.add("userId", String.valueOf(user.getId()));
				BookmarkablePageLink<ViewUserPage> viewUserLink = new BookmarkablePageLink<ViewUserPage>("view-user",
						ViewUserPage.class, actionsParameters);
				item.add(viewUserLink);
				viewUserLink.add(new Label("user-name", user.getUsername()));
				Link<Void> switchLockedStatusLink = new Link<Void>("switch-locked-status") {
					@Override
					public void onClick() {
						if (user.isAccountLocked()) {
							userService.unlockUser(user.getId());
							info("User unlocked: " + user.getUsername());
						} else {
							RoutineResult result = userService.lockUser(user.getId());
							if (result.isOk()) {
								info("User locked: " + user.getUsername() + "; please be aware that some sessions could be expired");
							} else {
								error("Error while trying to lock a user: " + result.getErrorMessage());
							}
						}
					}
				};
				switchLockedStatusLink.add(new Label("locked-status", user.isAccountLocked() ? "Yes" : "No"));
				item.add(switchLockedStatusLink);
				item.add(new Label("logins-failed", String.valueOf(user.getLoginsFailed())));
				item.add(DateLabels.forDateTime("last-login-date", user.getLastLoginDate()));
				item.add(new Label("email",user.getEmail()));
				
				item.add(new BookmarkablePageLink<EditUserPage>("edit-user",
						EditUserPage.class, actionsParameters));
				item.add(new BookmarkablePageLink<ChangeUserRolesPage>("change-user-roles",
						ChangeUserRolesPage.class, actionsParameters));
				item.add(new BookmarkablePageLink<ChangeUserActionsPage>("change-user-actions",
						ChangeUserActionsPage.class, actionsParameters));
				item.add(new BookmarkablePageLink<CloneUserPage>("clone-user",
						CloneUserPage.class, actionsParameters));
			}
		};
		add(usersDataView);

		// ordering, paging...
		add(new OrderByLink("order-by-username", "username", usersDataProvider));
		add(new OrderByLink("order-by-locked", "locked", usersDataProvider));
		add(new OrderByLink("order-by-logins-failed", "loginsFailed", usersDataProvider));
		add(new OrderByLink("order-by-last-login-date", "lastLoginDate", usersDataProvider));
		
		add(new PagingNavigator("paging-navigator", usersDataView));
		
		add(new BookmarkablePageLink<AddUserPage>("add-user", AddUserPage.class));
	}
	
	@Override
	protected String getTitle() {
		return "Users";
	}

	@SuppressWarnings("unused")
	private class UserFilters implements Serializable {
		private String usernamePrefix;
		private UIRoleForFilter role;
		private UISubsystemForFilter subsystem;

		public String getUsernamePrefix() {
			return usernamePrefix;
		}

		public void setUsernamePrefix(String usernamePrefix) {
			this.usernamePrefix = usernamePrefix;
		}

		public UIRoleForFilter getRole() {
			return role;
		}

		public void setRole(UIRoleForFilter role) {
			this.role = role;
		}

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}
	}

}
