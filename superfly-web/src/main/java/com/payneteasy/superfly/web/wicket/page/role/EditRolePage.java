package com.payneteasy.superfly.web.wicket.page.role;

import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

@Secured("ROLE_ADMIN")
public class EditRolePage extends BasePage {
	@SpringBean
	private RoleService roleService;

	public EditRolePage(PageParameters parameters) {
		super(ListRolesPage.class, parameters);
		 long roleId = parameters.get("id").toLong(-1L);
		 final UIRole role  = roleService.getRole(roleId);
		 setDefaultModel(new CompoundPropertyModel<UIRole>(role));
		 Form form = new Form("form"){

			@Override
			protected void onSubmit() {
			roleService.updateRole(role);
			setResponsePage(ListRolesPage.class);
			}
			 
		 };
		 add(form);
		 form.add(new LabelValueRow<String>("role-name", new Model<String>(role.getRoleName()), "role.create.name"));
		 form.add(new LabelTextFieldRow<String>(role, "principalName", "role.create.principal-name", true));		 
		 form.add(new Button("form-submit"));
	     form.add(new BookmarkablePageLink<Page>("form-cancel", ListRolesPage.class));
	}

	@Override
	protected String getTitle() {
		return "Edit role";
	}

}
