package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.action.UILinkedAction;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;

/**
 * Displays user's properties.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
@Deprecated
public class ViewUserPage extends BasePage {
	
	@SpringBean
	private UserService userService;
	@SpringBean
	private SubsystemService subsystemService;
	
	private IModel<UIUserWithRolesAndActions> userModel;
	private IModel<List<UIRoleWithActions>> rolesModel;

	public ViewUserPage(PageParameters params) {
		super(params);
		
		final long userId = params.getAsLong("userId");
		
		final Filters filters = new Filters();
		Form<Filters> filtersForm = new Form<Filters>("filters-form") {
			public void onSubmit() {
				userModel.detach();
				rolesModel.detach();
			}
		};
		add(filtersForm);
		DropDownChoice<UISubsystemForFilter> subsystemDropdown = new DropDownChoice<UISubsystemForFilter>("subsystem-filter",
				new PropertyModel<UISubsystemForFilter>(filters, "subsystem"),
				subsystemService.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystemDropdown.setNullValid(true);
		filtersForm.add(subsystemDropdown);
		filtersForm.add(new TextField<String>("role-name-filter",
				new PropertyModel<String>(filters, "roleNameSubstring")));
		filtersForm.add(new TextField<String>("action-name-filter",
				new PropertyModel<String>(filters, "actionNameSubstring")));
		
		userModel = new LoadableDetachableModel<UIUserWithRolesAndActions>() {
			@Override
			protected UIUserWithRolesAndActions load() {
				UISubsystemForFilter subsystem = filters.getSubsystem();
				return userService.getUserRoleActions(userId,
						subsystem == null ? null : String.valueOf(subsystem.getId()),
						filters.getActionNameSubstring(), filters.getRoleNameSubstring());
			}
		};
		
		rolesModel = new LoadableDetachableModel<List<UIRoleWithActions>>() {
			@Override
			public List<UIRoleWithActions> load() {
				return userModel.getObject().getRoles();
			}
		};
		
		add(new Label("user-name", userModel.getObject().getName()));
		
		ListView<UIRoleWithActions> rolesListView = new ListView<UIRoleWithActions>("roles", rolesModel) {
			@Override
			protected void populateItem(ListItem<UIRoleWithActions> item) {
				final UIRoleWithActions role = item.getModelObject();
				item.add(new Label("subsystem-name", role.getSubsystemName()));
				item.add(new Label("role-name", role.getName()));
				item.add(new ListView<UILinkedAction>("actions", role.getActions()) {
					@Override
					protected void populateItem(ListItem<UILinkedAction> innerItem) {
						final UILinkedAction action = innerItem.getModelObject();
						innerItem.add(new Label("action-name", action.getName()));
					}
				});
			}
		};
		add(rolesListView);
		
		add(new BookmarkablePageLink<ListUsersPage>("cancel-link", ListUsersPage.class));
	}
	
	@Override
	protected String getTitle() {
		return "User details";
	}

	@SuppressWarnings("unused")
	private class Filters implements Serializable {
		private UISubsystemForFilter subsystem;
		private String roleNameSubstring;
		private String actionNameSubstring;

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}

		public String getRoleNameSubstring() {
			return roleNameSubstring;
		}

		public void setRoleNameSubstring(String roleNameSubstring) {
			this.roleNameSubstring = roleNameSubstring;
		}

		public String getActionNameSubstring() {
			return actionNameSubstring;
		}

		public void setActionNameSubstring(String actionNameSubstring) {
			this.actionNameSubstring = actionNameSubstring;
		}

	}

}
