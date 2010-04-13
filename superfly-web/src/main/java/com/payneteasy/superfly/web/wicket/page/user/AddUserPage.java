package com.payneteasy.superfly.web.wicket.page.user;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

/**
 * Page used to create a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class AddUserPage extends BasePage {
	
	@SpringBean
	private UserService userService;

	public AddUserPage() {
		super();
		final UIUserWithPassword2 user = new UIUserWithPassword2();
		Form<UIUserWithPassword2> form = new Form<UIUserWithPassword2>("form", new Model<UIUserWithPassword2>(user)) {
			@Override
			protected void onSubmit() {
				//userService.createUser(user);
				PageParameters params = new PageParameters();
				params.add("userId", String.valueOf(user.getId()));
				params.add("wizard", "true");
				getRequestCycle().setResponsePage(ChangeUserRolesPage.class, params);
				getRequestCycle().setRedirect(true);
				info("User created: " + user.getUsername());
			}
		};
		add(form);
		form.add(new RequiredTextField<String>("username",
				new PropertyModel<String>(user, "username")));
		TextField<String> email = new TextField<String>("email",new PropertyModel<String>(user,"email"));
		email.add(EmailAddressValidator.getInstance());
		form.add(email.setRequired(true));
		FormComponent<String> password1Field = new PasswordTextField("password",
				new PropertyModel<String>(user, "password")).setRequired(true);
		form.add(password1Field);
		FormComponent<String> password2Field = new PasswordTextField("password2",
				new PropertyModel<String>(user, "password2")).setRequired(true);
		form.add(password2Field);
		form.add(new EqualPasswordInputValidator(password1Field, password2Field));
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}
	
	@Override
	protected String getTitle() {
		return "Add user";
	}

}
