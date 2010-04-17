package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

/**
 * Used to change roles assigned to a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class ChangeUserRolesPage extends BasePage {

	@SpringBean
	private UserService userService;
	@SpringBean
	private SubsystemService subsystemService;

	public ChangeUserRolesPage(PageParameters params) {
		super(params);

		final long userId = params.getAsLong("userId");
		final long subId = params.getAsLong("subId");

		List<UIRoleForCheckbox> roles = userService.getUnmappedUserRoles(
				userId, subId, 0, Integer.MAX_VALUE);
		UIUser user = userService.getUser(userId);
		UISubsystem subsystem = subsystemService.getSubsystem(subId);
		add(new Label("user-name",user.getUsername()));
		add(new Label("sub-name",subsystem.getName()));
		Form<Void> form = new Form<Void>("form");
		add(form);
		final ListRole listRole = new ListRole();
		DropDownChoice<UIRoleForCheckbox> roleDropdown = new DropDownChoice<UIRoleForCheckbox>("role-filter",
				new PropertyModel<UIRoleForCheckbox>(listRole, "role"),
				roles,new com.payneteasy.superfly.web.wicket.page.user.RoleChoiceRenderer());
		form.add(roleDropdown);
		form.add(new Button("add-role"){

			@Override
			public void onSubmit() {
				List<Long> rolesId = new ArrayList<Long>();
				rolesId.add(listRole.getRole().getId());
				RoutineResult result = userService.changeUserRoles(userId, rolesId, null, null);
				if (result.isOk()) 
				{
					info("Roles changed; please be aware that some sessions could be invalidated");
					} else { error("Error while changing user roles: " + result.getErrorMessage());}
				PageParameters parameters = new PageParameters();
				parameters.add("userId", String.valueOf(userId));
				setResponsePage(UserDetailsPage.class, parameters);
			}
			
		});
		form.add(new Button("cancel"){

			@Override
			public void onSubmit() {
				PageParameters parameters = new PageParameters();
				parameters.add("userId", String.valueOf(userId));
				setResponsePage(UserDetailsPage.class, parameters);
			}
			
		});
	}

	@Override
	protected String getTitle() {
		return "add roles";
	}

	@SuppressWarnings("unused")
	private class ListRole implements Serializable {
		private UIRoleForCheckbox role;

		public UIRoleForCheckbox getRole() {
			return role;
		}

		public void setRole(UIRoleForCheckbox role) {
			this.role = role;
		}

	}
}
