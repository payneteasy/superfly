package com.payneteasy.superfly.web.wicket.page.user;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.beans.BeanUtils;
import org.springframework.security.annotation.Secured;

import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.BasePage;

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
		
		long userId = params.getAsLong("userId", -1L);
		
		final UIUser initialUser = userService.getUser(userId);
		final UIUserWithPassword2 user = new UIUserWithPassword2();
		BeanUtils.copyProperties(initialUser, user);
		// we don't want to send the password to the page
		user.setPassword(null);
		Form<UIUserWithPassword2> form = new Form<UIUserWithPassword2>("form", new Model<UIUserWithPassword2>(user)) {
			@Override
			protected void onSubmit() {
				userService.updateUser(user);
				getRequestCycle().setResponsePage(ListUsersPage.class);
				getRequestCycle().setRedirect(true);
				info("User updated: " + user.getUsername());
			}
		};
		add(form);
		form.add(new Label("username", new PropertyModel<String>(user, "username")));
		FormComponent<String> password1Field = new PasswordTextField("password",
				new PropertyModel<String>(user, "password"));
		form.add(password1Field);
		FormComponent<String> password2Field = new PasswordTextField("password2",
				new PropertyModel<String>(user, "password2"));
		form.add(password2Field);
		form.add(new EqualPasswordInputValidator(password1Field, password2Field));
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}
	
	@Override
	protected String getTitle() {
		return "Edit user";
	}

}
