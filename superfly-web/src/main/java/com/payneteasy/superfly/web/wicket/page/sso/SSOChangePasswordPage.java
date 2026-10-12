package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPanel;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page used to change a temporary password. It is the last step of a login that has accepted the temporary
 * password and passed the second factor (or needs none), see {@link SSOUtils#onSecondFactorPassed}.
 */
public class SSOChangePasswordPage extends BaseSSOPage {

    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;

    public SSOChangePasswordPage(final String username) {
        if (!isPasswordChangeAllowed(username)) {
            throw new RestartResponseException(new SSOLoginErrorPage(new Model<>("No login data found")));
        }

        add(new ChangePasswordPanel("change-password-panel") {
            @Override
            protected Component createCsrfInput(String aMarkupId) {
                return createCsrfHiddenInput(aMarkupId);
            }

            @Override
            protected boolean validateCsrf() {
                if (!validateCsrfToken()) {
                    return false;
                }
                // the login may have been completed or restarted since the page was shown;
                // this is the last check before the password is changed
                if (!isPasswordChangeAllowed(username)) {
                    SSOUtils.redirectToLoginErrorPage(SSOChangePasswordPage.this, new Model<>("No login data found"));
                    return false;
                }
                return true;
            }

            @Override
            protected String getCurrentUserName() {
                return username;
            }

            @Override
            protected void onPasswordChanged() {
                renewSessionId(SSOChangePasswordPage.this);
                SSOLoginData loginData = SSOChangePasswordPage.this.getSession().getSsoLoginData();
                loginData.setPasswordChangeRequired(false);
                loginData.setPasswordChangeAllowed(false);
                // the second factor has been passed before the change, so the login is complete
                SSOUtils.onSuccessfulLogin(username,
                        SSOChangePasswordPage.this,
                        loginData,
                        sessionService, subsystemService);
            }
        });
    }

    private boolean isPasswordChangeAllowed(String username) {
        SSOLoginData loginData = getSession().getSsoLoginData();
        return loginData != null && loginData.isPasswordChangeAllowed()
                && username != null && username.equals(loginData.getUsername());
    }

}
