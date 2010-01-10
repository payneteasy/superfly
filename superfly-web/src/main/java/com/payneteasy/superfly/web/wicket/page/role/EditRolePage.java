package com.payneteasy.superfly.web.wicket.page.role;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

public class EditRolePage extends BasePage {
	@SpringBean
	private RoleService roleService;

	public EditRolePage(PageParameters parameters) {
		super(parameters);
		 long roleId = parameters.getAsLong("id", -1L);
		 final UIRole role  = roleService.getRole(roleId);
		 setDefaultModel(new CompoundPropertyModel<UIRole>(role));
		 Form form = new Form("form"){

			@Override
			protected void onSubmit() {
			roleService.updateRole(role);
			setResponsePage(ListRolePage.class);
			}
			 
		 };
		 add(form);
		 form.add(new RequiredTextField<String>("principalName"));
		 form.add(new Button("form-submit"));
	       form.add(new Button("form-cancel"){

			@Override
			public void onSubmit() {
				setResponsePage(ListRolePage.class);
			}
	    	   
	       });
	}

	@Override
	protected String getTitle() {
		return "Edit role";
	}

}
