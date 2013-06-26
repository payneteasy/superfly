package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextAreaRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PublicKeyValidator;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.annotation.Secured;

/**
 * Page used to edit a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class EditUserPage extends BasePage {

	@SpringBean
	private UserService userService;
	@SpringBean
	private PublicKeyCrypto crypto;

	public EditUserPage(PageParameters params) {
		super(ListUsersPage.class, params);

		long userId = params.get("userId").toLong();

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
				info("User updated: " + user.getUsername());
			}
		};
		add(form);

		LabelTextFieldRow<String> userName = new LabelTextFieldRow<String>(user,"username","user.create.username",true);
		form.add(userName);

		LabelTextFieldRow<String> email = new LabelTextFieldRow<String>(user, "email", "user.create.email", true);
		email.getTextField().add(EmailAddressValidator.getInstance());
		form.add(email);
		
//		LabelPasswordTextFieldRow password1Field = new LabelPasswordTextFieldRow(user, "password", "user.create.password", true);
//		form.add(password1Field);
//		
//		LabelPasswordTextFieldRow password2Field = new LabelPasswordTextFieldRow(user, "password2", "user.create.password2", true);
//		form.add(password2Field);
//		
//		form.add(new EqualPasswordInputValidator(password1Field.getPasswordTextField(), password2Field.getPasswordTextField()));
//		
//		form.add(new PasswordInputValidator(userName.getTextField(), password1Field.getPasswordTextField(), userService));
		
		LabelTextAreaRow<String> publicKeyField = new LabelTextAreaRow<String>(user, "publicKey", "user.create.publicKey");
		publicKeyField.getTextField().add(new PublicKeyValidator(crypto));
		form.add(publicKeyField);
		
		form.add(new LabelTextFieldRow<String>(user,"name","user.create.name", true));

		form.add(new LabelTextFieldRow<String>(user,"surname","user.create.surname", true));

		form.add(new LabelTextFieldRow<String>(user,"secretQuestion","user.create.secret-question", true));

		form.add(new LabelTextFieldRow<String>(user,"secretAnswer","user.create.secret-answer", true));

		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}

	@Override
	protected String getTitle() {
		return "Edit user";
	}

}
