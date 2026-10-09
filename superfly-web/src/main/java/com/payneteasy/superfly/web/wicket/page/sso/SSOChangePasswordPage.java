package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

/**
 * Page used to change a password.
 */
public class SSOChangePasswordPage extends BaseSSOPage {

    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private UserService userService;

    public SSOChangePasswordPage(final String username) {
        if (getSession().getSsoLoginData() == null) {
            SSOUtils.redirectToLoginErrorPage(this, new Model<String>("No login data found"));
        }

        add(new ChangePasswordPanel("change-password-panel") {
            @Override
            protected Component createCsrfInput(String aMarkupId) {
                return createCsrfHiddenInput(aMarkupId);
            }

            @Override
            protected boolean validateCsrf() {
                return validateCsrfToken();
            }

            @Override
            protected String getCurrentUserName() {
                return username;
            }

            @Override
            protected void onPasswordChanged() {
                renewSessionId(SSOChangePasswordPage.this);
                UserForDescription user = userService.getUserForDescription(username);
                SSOLoginData loginData = SSOChangePasswordPage.this.getSession().getSsoLoginData();
                if (loginData != null) {
                    loginData.setUsername(username);
                    loginData.setOtpTypeCode(user.getOtpTypeCode());
                    loginData.setOtpOptional(user.isOtpOptional());
                    loginData.setGoogleAuthSetupRequired(false);
                }

                boolean hasKey = OTPType.GOOGLE_AUTH.equals(user.getOtpType())
                        && StringUtils.hasLength(userService.getOtpMasterKeyByUsername(username));
                if (OTPType.GOOGLE_AUTH.equals(user.getOtpType()) && (hasKey || !user.isOtpOptional())) {
                    // an existing key must never be replaced without passing OTP
                    if (hasKey) {
                        getRequestCycle().setResponsePage(new SSOLoginHOTPPage());
                    } else {
                        if (loginData != null) {
                            loginData.setGoogleAuthSetupRequired(true);
                        }
                        getRequestCycle().setResponsePage(new SSOSetupGoogleAuthPage());
                    }
                } else {
                    SSOUtils.onSuccessfulLogin(username,
                            SSOChangePasswordPage.this,
                            loginData,
                            sessionService, subsystemService);
                }
            }
        });
    }

}