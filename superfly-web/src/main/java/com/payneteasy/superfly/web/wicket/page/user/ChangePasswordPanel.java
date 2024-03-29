package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.validation.PasswordInputValidator;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel used to reset a password.
 */
public abstract class ChangePasswordPanel extends Panel {

    @SpringBean
    private UserService userService;
    @SpringBean
    private SettingsService settingsService;

    public ChangePasswordPanel(String id) {
        super(id);



        add(new FeedbackPanel("feedback"));
        
        final UIUserCheckPassword user = new UIUserCheckPassword();

        user.setUsername(getCurrentUserName());

        final Form<UIUserCheckPassword> form = new Form<UIUserCheckPassword>("form",
                new Model<UIUserCheckPassword>(user)) {
                   @Override
                    public void onSubmit() {
                       if (validateCsrf()) {
                           userService.changeTempPassword(user.getUsername(), user.getPassword());
                           onPasswordChanged();
                       }
                    }
        };

        add(form);

        form.add(new WebMarkupContainer("password-description")
                .setVisible(settingsService.getPolicy() == Policy.PCIDSS));

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
        form.add(new PasswordInputValidator(getCurrentUserName(), password1Field, userService));

        form.add(createCsrfInput("_csrf"));

        Button submitButton = new Button("change");
        submitButton.setOutputMarkupId(true);
        submitButton.setMarkupId("change-button");
        form.add(submitButton);
    }

    protected abstract Component createCsrfInput(String aMarkupId);

    protected abstract boolean validateCsrf();

    protected abstract String getCurrentUserName();

    protected abstract void onPasswordChanged();

}