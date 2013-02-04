package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page used to change a password.
 */
public class SSOChangePasswordPage extends BaseSSOPage {

    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;

	public SSOChangePasswordPage(final String username) {
        if (getSession().getSsoLoginData() == null) {
            SSOUtils.redirectToLoginErrorPage(this, new Model<String>("No login data found"));
        }

        add(new ChangePasswordPanel("change-password-panel") {
            @Override
            protected String getCurrentUserName() {
                return username;
            }

            @Override
            protected void onPasswordChanged() {
                SSOUtils.onSuccessfulLogin(username,
                        SSOChangePasswordPage.this,
                        SSOChangePasswordPage.this.getSession().getSsoLoginData(),
                        sessionService, subsystemService);
            }
        });
	}

}