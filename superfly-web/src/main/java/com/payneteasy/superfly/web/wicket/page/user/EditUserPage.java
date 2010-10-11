package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.security.annotation.Secured;

/**
 * Page used to edit a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class EditUserPage extends BasePage {

	@SpringBean
	private UserService userService;


	public EditUserPage(PageParameters params) {
		super(params);

		long userId = params.getAsLong("userId");

		final UIUser initialUser = userService.getUser(userId);
		final UIUserWithPassword2 user = new UIUserWithPassword2();
		BeanUtils.copyProperties(initialUser, user);
		// we don't want to send the password to the page
		user.setPassword(null);
		Form<UIUserWithPassword2> form = new Form<UIUserWithPassword2>("form",
				new Model<UIUserWithPassword2>(user)) {
			@Override
			protected void onSubmit() {
				userService.updateUser(user);
				getRequestCycle().setResponsePage(ListUsersPage.class);
				getRequestCycle().setRedirect(true);
				info("User updated: " + user.getUsername());
			}
		};
		add(form);


        form.add(new Label("username", new PropertyModel<String>(user,
                "username")));

		TextField<String> email = new TextField<String>("email",
				new PropertyModel<String>(user, "email"));
		email.add(EmailAddressValidator.getInstance());
		form.add(email.setRequired(true));
		FormComponent<String> password1Field = new PasswordTextField(
				"password", new PropertyModel<String>(user, "password"));
		form.add(password1Field);
		FormComponent<String> password2Field = new PasswordTextField(
				"password2", new PropertyModel<String>(user, "password2"));
		form.add(password2Field);
		form.add(new EqualPasswordInputValidator(password1Field, password2Field));

        form.add(new PasswordInputValidator(user.getUsername(),password1Field,userService));

		form.add(new RequiredTextField<String>("name",
				new PropertyModel<String>(user, "name")));

		form.add(new RequiredTextField<String>("surname",
				new PropertyModel<String>(user, "surname")));

		form.add(new RequiredTextField<String>("secretQuestion",
				new PropertyModel<String>(user, "secretQuestion")));

		form.add(new RequiredTextField<String>("secretAnswer",
				new PropertyModel<String>(user, "secretAnswer")));

		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}

	@Override
	protected String getTitle() {
		return "Edit user";
	}

}
