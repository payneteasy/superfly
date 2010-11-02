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

import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.service.UserService;
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
		super(params);
		
		final long userId = params.getAsLong("userId");
		final UIUser oldUser = userService.getUser(userId);
		
		final UIUserWithPassword2 user = new UIUserWithPassword2();
		Form<UIUserWithPassword2> form = new Form<UIUserWithPassword2>("form", new Model<UIUserWithPassword2>(user)) {
			@Override
			protected void onSubmit() {
				userService.cloneUser(userId, user.getUsername(), user.getPassword(), user.getEmail(), user.getPublicKey());
				getRequestCycle().setResponsePage(ListUsersPage.class);
				getRequestCycle().setRedirect(true);
				info("User cloned: " + oldUser.getUsername() + " to " + user.getUsername());
			}
		};
		add(form);
		form.add(new Label("old-userid", String.valueOf(oldUser.getId())));
		form.add(new Label("old-username", oldUser.getUsername()));

        FormComponent<String> userName=new RequiredTextField<String>("username",
				new PropertyModel<String>(user, "username"));

		form.add(userName);
		TextField<String> email = new TextField<String>("email",new PropertyModel<String>(user, "email"));
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
        form.add(new PasswordInputValidator(userName,password1Field,userService));
        
        TextArea<String> publicKeyField = new TextArea<String>("public-key",
        		new PropertyModel<String>(user, "publicKey"));
        form.add(publicKeyField);
        publicKeyField.add(new PublicKeyValidator(crypto));
	}
	
	@Override
	protected String getTitle() {
		return "Clone user";
	}

}
