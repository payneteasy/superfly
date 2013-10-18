package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.user.UserCreationResult;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.RoleInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.SubsystemChoiceRenderer;
import com.payneteasy.superfly.web.wicket.component.field.LabelDropDownChoiceRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelPasswordTextFieldRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextAreaRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;
import com.payneteasy.superfly.web.wicket.validation.PublicKeyValidator;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.security.access.annotation.Secured;

import java.util.*;

@Secured("ROLE_ADMIN")
public class CreateUserPage extends BasePage {
	@SpringBean
	private UserService userService;
	@SpringBean
	private RoleService roleService;
	@SpringBean
	private SubsystemService subsystemService;
	@SpringBean
	private PublicKeyCrypto crypto;

	public CreateUserPage() {
		super(ListUsersPage.class);
		final UIUserCheckPassword user = new UIUserCheckPassword();

		List<UISubsystemForFilter> listSub = subsystemService.getSubsystemsForFilter();
		for (UISubsystemForFilter sub : listSub) {
			List<Long> listIdsub = new ArrayList<Long>();
			listIdsub.add(sub.getId());
			List<UIRoleForList> listRole = roleService.getRoles(0, Integer.MAX_VALUE, 1, true, null, listIdsub);
			subsystemsToRoles.put(sub, listRole);
		}

		// models for DropDrownChoice
		IModel<List<? extends UISubsystemForFilter>> subsystemsModel = new AbstractReadOnlyModel<List<? extends UISubsystemForFilter>>() {
			@Override
			public List<UISubsystemForFilter> getObject() {
				Set<UISubsystemForFilter> keys = subsystemsToRoles.keySet();
				List<UISubsystemForFilter> list = new ArrayList<UISubsystemForFilter>(keys);
				return list;
			}

		};
		final IModel<List<? extends UIRoleForList>> rolesModel = new LoadableDetachableModel<List<? extends UIRoleForList>>() {
			@Override
			public List<UIRoleForList> load() {
				List<UIRoleForList> models = subsystemsToRoles.get(subsystem);
				if (models == null) {
					models = Collections.emptyList();
				}
				return models;
			}

		};

		final Form<UIUserCheckPassword> form = new Form<UIUserCheckPassword>("form", new Model<UIUserCheckPassword>(user));
		add(form);

		LabelTextFieldRow<String> userName = new LabelTextFieldRow<String>(user,"username","user.create.username",true);
		form.add(userName);

		LabelTextFieldRow<String> email = new LabelTextFieldRow<String>(user, "email", "user.create.email", true);
		email.getTextField().add(EmailAddressValidator.getInstance());
		form.add(email);
		
		LabelPasswordTextFieldRow password1Field = new LabelPasswordTextFieldRow(user, "password", "user.create.password", true);
		form.add(password1Field);
		
		LabelPasswordTextFieldRow password2Field = new LabelPasswordTextFieldRow(user, "password2", "user.create.password2", true);
		form.add(password2Field);
		
		form.add(new EqualPasswordInputValidator(password1Field.getPasswordTextField(), password2Field.getPasswordTextField()));
		
		form.add(new PasswordInputValidator(userName.getTextField(), password1Field.getPasswordTextField(), userService));
		
		LabelTextAreaRow<String> publicKeyField = new LabelTextAreaRow<String>(user, "publicKey", "user.create.publicKey");
		publicKeyField.getTextField().add(new PublicKeyValidator(crypto));
		form.add(publicKeyField);

		LabelDropDownChoiceRow<UISubsystemForFilter> subsystemsRow = new LabelDropDownChoiceRow<UISubsystemForFilter>(
				"subsystem", this, "user.create.choice-subsystem",
				subsystemsModel, new SubsystemChoiceRenderer());
		subsystemsRow.getDropDownChoice().setRequired(true);
		
		final LabelDropDownChoiceRow<UIRoleForList> rolesRow = new LabelDropDownChoiceRow<UIRoleForList>("role", new Model<UIRoleForList>(), "user.create.choice-roles", rolesModel, new RoleInCreateUserChoiceRender());
		rolesRow.getDropDownChoice().setRequired(true);
		rolesRow.setOutputMarkupId(true);
		
		form.add(subsystemsRow);
		form.add(rolesRow);
		subsystemsRow.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onchange"){

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
                rolesModel.detach();
				target.add(rolesRow);
			}
			
		});
		
		form.add(new LabelTextFieldRow<String>(user,"name","user.create.name", true));

		form.add(new LabelTextFieldRow<String>(user,"surname","user.create.surname", true));

		form.add(new LabelTextFieldRow<String>(user,"secretQuestion","user.create.secret-question", true));

		form.add(new LabelTextFieldRow<String>(user,"secretAnswer","user.create.secret-answer", true));

        form.add(new LabelTextFieldRow<String>(user,"organization","user.create.organization", false));

		form.add(new Button("add") {

			@Override
			public void onSubmit() {
				UIRoleForList role = rolesRow.getDropDownChoice().getModelObject();
				user.setRoleId(role.getId());
                UserCreationResult result = userService.createUser(user, subsystem == null ? null : subsystem.getName());
                if (result.getMailSendError() != null) {
                    warn("Could not send a message: " + result.getMailSendError());
                }
                if (result.getResult().isOk()) {
                    getRequestCycle().setResponsePage(ListUsersPage.class);
                    info("User created: " + user.getUsername());
                } else if (result.getResult().isDuplicate()) {
                    error("User with this username already exists, please pick another one");
                } else {
                    error("Can't create user");
                }
			}

		});
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}

	@Override
	protected String getTitle() {
		return "Add user";
	}

	// DropDrownChoice
	private final Map<UISubsystemForFilter, List<UIRoleForList>> subsystemsToRoles = new HashMap<UISubsystemForFilter, List<UIRoleForList>>(); // map:company->model
	private UIRoleForList role;
	private UISubsystemForFilter subsystem;

	public UIRoleForList getRole() {
		return role;
	}

	public void setRole(UIRoleForList role) {
		this.role = role;
	}

	public UISubsystemForFilter getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForFilter subsystem) {
		this.subsystem = subsystem;
	}

}
