package com.payneteasy.superfly.web.wicket.page.user;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.HomePage;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;

/**
 * Page used to reset a password.
 */
@Secured("ROLE_ACTION_TEMP_PASSWORD")
public class ChangePasswordPage extends BasePage {

	@SpringBean
	private UserService userService;

	public ChangePasswordPage(PageParameters params) {
		super(params);
        
        final UIUserCheckPassword user = new UIUserCheckPassword();

        user.setUsername(SecurityUtils.getUsername());

        final Form<UIUserCheckPassword> form = new Form<UIUserCheckPassword>(
                "form", new Model<UIUserCheckPassword>(user));

        add(form);

        FormComponent<String> password1Field = new PasswordTextField(
                "password", new PropertyModel<String>(user, "password"))
                .setRequired(true);
        form.add(password1Field);

        FormComponent<String> password2Field = new PasswordTextField(
                "password2", new PropertyModel<String>(user, "password2"))
                .setRequired(true);
        form.add(password2Field);

        form.add(new EqualPasswordInputValidator(password1Field,
                        password2Field));


        form.add(new PasswordInputValidator(SecurityUtils.getUsername(), password1Field, userService));



        form.add(new Button("change") {

            @Override
            public void onSubmit() {
                userService.changeTempPassword(user.getUsername(),user.getPassword());
                getRequestCycle().setRedirect(true);
                getRequestCycle().setResponsePage(HomePage.class);
                getRequestCycle().getSession().invalidate();
                SecurityContextHolder.clearContext();
            }

        });
	}

	@Override
	protected String getTitle() {
		return "Change user password";
	}

}