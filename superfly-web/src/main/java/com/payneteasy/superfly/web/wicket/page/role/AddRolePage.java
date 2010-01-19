package com.payneteasy.superfly.web.wicket.page.role;

import java.io.Serializable;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class AddRolePage extends BasePage {
	@SpringBean
	RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;

	public AddRolePage() {
		super();
		final UIRole role = new UIRole();
		setDefaultModel(new CompoundPropertyModel<UIRole>(role));
		final RoleFilter roleFilter = new RoleFilter();
		Form form = new Form("form") {

		};
		add(form);
		form.add(new DropDownChoice<UISubsystemForFilter>(
				"subsystem-filter", new PropertyModel<UISubsystemForFilter>(
						roleFilter, "subsystem"), subsystemService
						.getSubsystemsForFilter(),
				new SubsystemChoiceRenderer()).setNullValid(true).setRequired(true));
		form.add(new RequiredTextField<String>("roleName"));
		form.add(new RequiredTextField<String>("principalName"));
		form.add(new Button("add-role") {

			@Override
			public void onSubmit() {
				role.setSubsystemId(roleFilter.getSubsystem().getId());
				roleService.createRole(role);
				PageParameters params = new PageParameters();
				params.add("id", String.valueOf(role.getRoleId()));
				params
						.add("idSubsystem", String.valueOf(role
								.getSubsystemId()));
				params.add("wizard", "true");
				getRequestCycle().setResponsePage(ChangeRoleGroupsPage.class,
						params);
				getRequestCycle().setRedirect(true);
			}

		});
		form.add(new Button("cancel") {

			@Override
			public void onSubmit() {
				setResponsePage(ListRolesPage.class);
			}

		}.setDefaultFormProcessing(false));
	}

	@Override
	protected String getTitle() {
		return "Create role";
	}

	@SuppressWarnings("unused")
	private class RoleFilter implements Serializable {
		private UISubsystemForFilter subsystem;

		public UISubsystemForFilter getSubsystem() {
			return subsystem;
		}

		public void setSubsystem(UISubsystemForFilter subsystem) {
			this.subsystem = subsystem;
		}

	}
}
