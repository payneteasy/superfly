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
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleWithActions;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
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

		UIUser user = userService.getUser(userId);
		UISubsystem subsystem = subsystemService.getSubsystem(subId);
		add(new Label("user-name",user.getUsername()));
		add(new Label("sub-name",subsystem.getName()));
		
		UIUserWithRolesAndActions user1 = userService.getUserRoleActions(
				userId, null, null, null);
		final List<UIRoleWithActions> roleWithAction = user1.getRoles();
		final SortRoleOfSubsystem sort = new SortRoleOfSubsystem();
		sort.setRoleWithAction(roleWithAction);
		List<UIRoleForCheckbox> rolesAll = userService.getUnmappedUserRoles(userId, subId, 0, Integer.MAX_VALUE);
		
		List<UIRoleWithActions> rolesChecked = sort.getRoles(subsystem.getName());
		List<UIRoleForCheckbox> selectedRole = new ArrayList<UIRoleForCheckbox>();
		List<UIRoleForCheckbox> notSelectedRole = new ArrayList<UIRoleForCheckbox>(); 
		for(UIRoleForCheckbox uic: rolesAll){
			for(UIRoleWithActions uirwa: rolesChecked){
				if(uic.getId()==uirwa.getId()){
					selectedRole.add(uic);
				}
			}
		}
		for(UIRoleForCheckbox uir:rolesAll){
			if(!selectedRole.contains(uir)){
				notSelectedRole.add(uir);
			}
		}
		
		Form<Void> form = new Form<Void>("form");
		add(form);
		final ListRole listRole = new ListRole();
		DropDownChoice<UIRoleForCheckbox> roleDropdown = (DropDownChoice<UIRoleForCheckbox>) new DropDownChoice<UIRoleForCheckbox>("role-filter",
				new PropertyModel<UIRoleForCheckbox>(listRole, "role"),
				notSelectedRole,new com.payneteasy.superfly.web.wicket.page.user.RoleChoiceRenderer()).setRequired(true);
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
			
		}.setDefaultFormProcessing(false));
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
