package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import com.payneteasy.superfly.web.wicket.page.login.LoginErrorPage;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOLoginPasswordPage extends SessionAccessorPage {
    @SpringBean
    private UserService userService;
    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOLoginPasswordPage() {
        final SSOLoginData loginData = getSsoLoginData();
        if (loginData == null) {
            RequestCycle.get().setRedirect(true);
            RequestCycle.get().setResponsePage(new LoginErrorPage(new Model<String>("No login data found")));
        }

        final LoginBean loginBean = new LoginBean();
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                doOnSubmit(loginBean, loginData);
            }
        };
        add(form);
        form.add(new TextField<String>("username", new PropertyModel<String>(loginBean, "username")));
        form.add(new PasswordTextField("password", new PropertyModel<String>(loginBean, "password")));

        errorMessageLabel = new Label("message", errorMessageModel);
        errorMessageLabel.setVisible(StringUtils.hasLength(errorMessageModel.getObject()));
        form.add(errorMessageLabel);
    }

    private void doOnSubmit(LoginBean loginBean, SSOLoginData loginData) {
        UserLoginStatus loginStatus = userService.getUserLoginStatus(
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
                // TODO: change password
                break;
        }
    }

    private void onPasswordChecked(LoginBean loginBean, SSOLoginData loginData) {
        SSOSession ssoSession = sessionService.createSSOSession(loginBean.getUsername());
        Cookie cookie = new Cookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, ssoSession.getIdentifier());
        cookie.setMaxAge(SSOUtils.SSO_SESSION_ID_COOKIE_MAXAGE);
        ((WebResponse) RequestCycle.get().getResponse()).addCookie(cookie);

        SubsystemTokenData token = subsystemService.issueSubsystemTokenIfCanLogin(
                ssoSession.getId(), loginData.getSubsystemIdentifier());
        if (token != null) {
            // can login: redirecting a user to a subsystem
            SSOUtils.redirectToSubsystem(this, loginData, token);
        } else {
            // can't login: just display an error
            // actually, this should not happen as we've already
            // checked user access, but just in case...
            SSOUtils.redirectToCantLoginErrorPage(this, loginData);
        }
    }

    public SSOLoginData getSsoLoginData() {
        return getSession().getSsoLoginData();
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
