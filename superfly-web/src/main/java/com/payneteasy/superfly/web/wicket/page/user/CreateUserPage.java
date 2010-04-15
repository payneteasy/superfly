package com.payneteasy.superfly.web.wicket.page.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.RoleInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.SubsystemInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.page.BasePage;

@Secured("ROLE_ADMIN")
public class CreateUserPage extends BasePage {
	@SpringBean
	private UserService userService;
	@SpringBean
	private RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;

	public CreateUserPage() {
		super();
		final UIUserCheckPassword user = new UIUserCheckPassword();

		List<UISubsystemForList> listSub = subsystemService.getSubsystems();
		for (UISubsystemForList sub : listSub) {
			List<Long> listIdsub = new ArrayList<Long>();
			listIdsub.add(sub.getId());
			List<UIRoleForList> listRole = roleService.getRoles(0,
					Integer.MAX_VALUE, 1, true, null, listIdsub);
			modelsMap.put(sub, listRole);
		}

		// models for DropDrownChoice
		IModel<List<? extends UISubsystemForList>> makeChoices = new AbstractReadOnlyModel<List<? extends UISubsystemForList>>() {
			@Override
			public List<UISubsystemForList> getObject() {
				Set<UISubsystemForList> keys = modelsMap.keySet();
				List<UISubsystemForList> list = new ArrayList<UISubsystemForList>(
						keys);
				return list;
			}

		};
		final IModel<List<? extends UIRoleForList>> modelChoices = new AbstractReadOnlyModel<List<? extends UIRoleForList>>() {
			@Override
			public List<UIRoleForList> getObject() {
				List<UIRoleForList> models = modelsMap.get(subsystem);
				if (models == null) {
					models = Collections.emptyList();
				}
				return models;
			}

		};

		final Form<UIUserCheckPassword> form = new Form<UIUserCheckPassword>(
				"form", new Model<UIUserCheckPassword>(user));

		add(form);
		form.add(new RequiredTextField<String>("username",
				new PropertyModel<String>(user, "username")));
		TextField<String> email = new TextField<String>("email",
				new PropertyModel<String>(user, "email"));
		email.add(EmailAddressValidator.getInstance());
		form.add(email.setRequired(true));
		FormComponent<String> password1Field = new PasswordTextField(
				"password", new PropertyModel<String>(user, "password"))
				.setRequired(true);
		form.add(password1Field);
		FormComponent<String> password2Field = new PasswordTextField(
				"password2", new PropertyModel<String>(user, "password2"))
				.setRequired(true);
		form.add(password2Field);
		form
				.add(new EqualPasswordInputValidator(password1Field,
						password2Field));
		// DropDownChoice
		final DropDownChoice<UISubsystemForList> makes = (DropDownChoice<UISubsystemForList>) new DropDownChoice<UISubsystemForList>(
				"makes", new PropertyModel<UISubsystemForList>(this,
						"subsystem"), makeChoices,new SubsystemInCreateUserChoiceRender()).setNullValid(true);

		final DropDownChoice<UIRoleForList> models = (DropDownChoice<UIRoleForList>) new DropDownChoice<UIRoleForList>(
				"models", new Model<UIRoleForList>(), modelChoices, new RoleInCreateUserChoiceRender()).setNullValid(true);
		models.setOutputMarkupId(true);

		form.add(makes);
		form.add(models);

		makes.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.addComponent(models);
			}
		});
		form.add(new Button("add") {

			@Override
			public void onSubmit() {
				UIRoleForList role = models.getModelObject();
				user.setRoleId(role.getId());
				userService.createUser(user);
				getRequestCycle().setResponsePage(ListUsersPage.class);
				getRequestCycle().setRedirect(true);
				info("User created: " + user.getUsername());
			}

		});
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}

	@Override
	protected String getTitle() {
		return "Add user";
	}

	// DropDrownChoice
	private final Map<UISubsystemForList, List<UIRoleForList>> modelsMap = new HashMap<UISubsystemForList, List<UIRoleForList>>(); // map:company->model
	private UIRoleForList role;
	private UISubsystemForList subsystem;

	public UIRoleForList getRole() {
		return role;
	}

	public void setRole(UIRoleForList role) {
		this.role = role;
	}

	public UISubsystemForList getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForList subsystem) {
		this.subsystem = subsystem;
	}

}
