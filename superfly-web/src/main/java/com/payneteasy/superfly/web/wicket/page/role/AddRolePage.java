package com.payneteasy.superfly.web.wicket.page.role;

import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;

@Secured("ROLE_ADMIN")
public class AddRolePage extends BasePage {
	@SpringBean
	RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;

	public AddRolePage() {
		super(ListRolesPage.class);
		
		final UIRole role = new UIRole();
		setDefaultModel(new CompoundPropertyModel<UIRole>(role));
		final RoleFilter roleFilter = new RoleFilter();
		Form<Void> form = new Form<Void>("form") {

		};
		add(form);
		LabelDropDownChoiceRow<UISubsystemForFilter> subsystem =new LabelDropDownChoiceRow<UISubsystemForFilter>("subsystem", roleFilter, "role.create.subsystem", subsystemService
				.getSubsystemsForFilter(), new SubsystemChoiceRenderer());
		subsystem.getDropDownChoice().setRequired(true);
		form.add(subsystem);
		
		form.add(new LabelTextFieldRow<String>(role, "roleName", "role.create.name", true));
		form.add(new LabelTextFieldRow<String>(role, "principalName", "role.create.principal-name", true));
		form.add(new Button("add-role") {

			@Override
			public void onSubmit() {
				role.setSubsystemId(roleFilter.getSubsystem().getId());
				roleService.createRole(role);
				PageParameters params = new PageParameters();
				params.set("id", String.valueOf(role.getRoleId()));
				params.set("idSubsystem", String.valueOf(role.getSubsystemId()));
				params.set("wizard", "true");
				getRequestCycle().setResponsePage(AddRoleGroupsPage.class,
                        params);
			}

		});
		form.add(new BookmarkablePageLink<Page>("cancel",ListRolesPage.class));
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
