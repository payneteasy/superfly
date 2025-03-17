package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOLoginPasswordPage extends BaseSSOPage {
    @SpringBean
    private UserService userService;
    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private SettingsService settingsService;

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOLoginPasswordPage() {
        final SSOLoginData loginData = SSOUtils.getSsoLoginData(this);
        if (loginData == null) {
            RequestCycle.get().setResponsePage(new SSOLoginErrorPage(new Model<>("No login data found")));
        }

        final LoginBean loginBean = new LoginBean();
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                if (validateCsrfToken()) {
                    doOnSubmit(loginBean, loginData);
                }
            }
        };
        add(form);
        form.add(new TextField<String>("username", new PropertyModel<String>(loginBean, "username")));
        form.add(new PasswordTextField("password", new PropertyModel<String>(loginBean, "password")));
        form.add(createCsrfHiddenInput("_csrf"));

        errorMessageLabel = new Label("message", errorMessageModel);
        errorMessageLabel.setVisible(StringUtils.hasLength(errorMessageModel.getObject()));
        form.add(errorMessageLabel);
    }

    @Override
    protected IModel<String> createCustomCssUrlModel() {
        return new SubsystemLoginCssUrlModel(
                subsystemService, getSession().getSsoLoginData());
    }

    private void doOnSubmit(LoginBean loginBean, SSOLoginData loginData) {
        UserLoginStatus loginStatus = userService.checkUserCanLoginWithThisPassword(
                loginBean.getUsername(), loginBean.getPassword(),
                loginData.getSubsystemIdentifier());
        switch (loginStatus) {
            case SUCCESS:
                onPasswordChecked(loginBean, loginData);
                break;
            case FAILED:
                errorMessageModel.setObject("The username or password you entered is incorrect or user is locked.");
                errorMessageLabel.setVisible(true);
                break;
            case TEMP_PASSWORD:
                getRequestCycle().setResponsePage(new SSOChangePasswordPage(loginBean.getUsername()));
                break;
        }
    }

    private void onPasswordChecked(LoginBean loginBean, SSOLoginData loginData) {
        loginData.setUsername(loginBean.getUsername());
        UserForDescription userDescription = userService.getUserForDescription(loginData.getUsername());
        OTPType otpType = userDescription.getOtpType();
        switch (otpType) {
            case GOOGLE_AUTH:
                if (userDescription.isOtpOptional()) {
                    SSOUtils.onSuccessfulLogin(loginBean.getUsername(),
                            this, loginData, sessionService, subsystemService);
                } else if (!StringUtils.hasLength(userService.getOtpMasterKeyByUsername(loginBean.getUsername()))) {
                    getRequestCycle().setResponsePage(new SSOSetupGoogleAuthPage());
                } else {
                    getRequestCycle().setResponsePage(new SSOLoginHOTPPage());
                }
                break;
            case NONE:
            default:
                SSOUtils.onSuccessfulLogin(loginBean.getUsername(),
                        this, loginData, sessionService, subsystemService);
        }

    }

    private static class LoginBean implements Serializable {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
