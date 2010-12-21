package com.payneteasy.superfly.web.wicket.page.user;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.security.access.annotation.Secured;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.component.field.LabelPasswordTextFieldRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextAreaRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelTextFieldRow;
import com.payneteasy.superfly.web.wicket.component.field.LabelValueRow;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;
import com.payneteasy.superfly.web.wicket.validation.PublicKeyValidator;

/**
 * Page used to create a user.
 * 
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_ADMIN")
public class CloneUserPage extends BasePage {
	
	@SpringBean
	private UserService userService;
	@SpringBean
	private PublicKeyCrypto crypto;

    @SpringBean
    private IPolicyValidation<PasswordCheckContext> policyValidation;

	public CloneUserPage(PageParameters params) {
		super(ListUsersPage.class, params);
		
		final long userId = params.getAsLong("userId");
		final UIUser oldUser = userService.getUser(userId);
		
		final UIUserWithPassword2 user = new UIUserWithPassword2();
		Form<UIUserWithPassword2> form = new Form<UIUserWithPassword2>("form", new Model<UIUserWithPassword2>(user)) {
			@Override
			protected void onSubmit() {
				try {
					userService.cloneUser(userId, user.getUsername(), user.getPassword(), user.getEmail(), user.getPublicKey());
				} catch (MessageSendException e) {
					error("Could not send a message: " + e.getMessage());
				}
				getRequestCycle().setResponsePage(ListUsersPage.class);
				getRequestCycle().setRedirect(true);
				info("User cloned: " + oldUser.getUsername() + " to " + user.getUsername());
			}
		};
		add(form);
		form.add(new LabelValueRow<String>("old-username", new Model(oldUser.getUsername()),"user.clone.template-name"));

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
		
		form.add(new BookmarkablePageLink<Page>("cancel", ListUsersPage.class));
	}
	
	@Override
	protected String getTitle() {
		return "Clone user";
	}

}
