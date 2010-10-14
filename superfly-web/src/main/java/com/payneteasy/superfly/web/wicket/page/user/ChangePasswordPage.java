package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.component.RoleInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.component.SubsystemInCreateUserChoiceRender;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.security.annotation.Secured;
import org.springframework.security.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Page used to edit a user.
 *
 * @author Roman Puchkovskiy
 */
@Secured("ROLE_TEMP_PASSWORD")
public class ChangePasswordPage extends BasePage {

	@SpringBean
	private UserService userService;

	public ChangePasswordPage(PageParameters params) {
		super(params);
        //SecurityContextHolder.clearContext();

        
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


        form.add(new PasswordInputValidator(SecurityUtils.getUsername(),password1Field,userService));



        form.add(new Button("change") {

            @Override
            public void onSubmit() {
                getRequestCycle().setRedirect(false);
                getRequestCycle().getSession().invalidate();
                SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
                SecurityContextHolder.clearContext();
            }

        });
	}

	@Override
	protected String getTitle() {
		return "Change user password";
	}

}