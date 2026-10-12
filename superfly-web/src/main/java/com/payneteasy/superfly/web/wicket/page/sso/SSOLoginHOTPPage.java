package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOLoginHOTPPage extends BaseSSOPage {
    private static final String CODE_DID_NOT_MATCH = "One-time password value did not match.";

    @SpringBean
    private InternalSSOService internalSSOService;
    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOLoginHOTPPage() {
        final SSOLoginData loginData = SSOUtils.getSsoLoginData(this);
        if (loginData == null || loginData.getUsername() == null) {
            throw new RestartResponseException(new SSOLoginErrorPage(new Model<String>("No login data found")));
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
        form.add(new PasswordTextField("hotp", new PropertyModel<String>(loginBean, "hotp")));
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
        if (isLoginBlocked("otp", loginData.getUsername())) {
            errorMessageModel.setObject("Too many failed login attempts. Try again later.");
            errorMessageLabel.setVisible(true);
            return;
        }
        CheckOtpResult.Status status = internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH,
                loginData.getUsername(), loginBean.getHotp());
        boolean ok = status == CheckOtpResult.Status.SUCCESS;
        recordLoginResult("otp", loginData.getUsername(), ok);
        if (ok) {
            onHOTPChecked(loginData);
        } else {
            errorMessageModel.setObject(failureMessage(status));
            errorMessageLabel.setVisible(true);
        }
    }

    /**
     * Why a code was refused. The page is reachable only after the password step, which puts the username
     * into the login data, so the reason is told to the user who has passed that step. A missing or an
     * unknown result is reported as a wrong code.
     */
    private static String failureMessage(CheckOtpResult.Status status) {
        if (status == null) {
            return CODE_DID_NOT_MATCH;
        }
        switch (status) {
            case ALREADY_USED:
                return "This code has already been used. Wait for the next code and enter it.";
            case CLOCK_SKEW:
                return "The time on your device is out of sync with the server. "
                        + "Synchronize the time on your device and enter a new code.";
            case LOCKED:
                return "The account is locked. Contact your administrator.";
            default:
                return CODE_DID_NOT_MATCH;
        }
    }

    private void onHOTPChecked(SSOLoginData loginData) {
        SSOUtils.onSuccessfulLogin(loginData.getUsername(),
                this, loginData, sessionService, subsystemService);
    }

    private static class LoginBean implements Serializable {
        private String hotp;

        public String getHotp() {
            return hotp;
        }

        public void setHotp(String hotp) {
            this.hotp = hotp;
        }
    }
}
