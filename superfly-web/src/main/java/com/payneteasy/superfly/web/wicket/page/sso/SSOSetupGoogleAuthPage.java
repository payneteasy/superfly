package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.web.wicket.component.otp.GoogleAuthSetupPanel;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

/**
 * @author rpuch
 */
public class SSOSetupGoogleAuthPage extends BaseSSOPage {
    @SpringBean
    private InternalSSOService internalSSOService;
    @SpringBean
    private SessionService sessionService;
    @SpringBean
    private SubsystemService subsystemService;
    @SpringBean
    private HOTPService hotpService;
    @SpringBean
    private UserService userService;

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOSetupGoogleAuthPage() {
        final SSOLoginData loginData = SSOUtils.getSsoLoginData(this);
        if (loginData == null || loginData.getUsername() == null || !loginData.isGoogleAuthSetupRequired()) {
            throw new RestartResponseException(new SSOLoginErrorPage(new Model<>("No login data found")));
        }

        IModel<String> masterKey = Model.of("");
        IModel<String> code = Model.of("");
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                if (validateCsrfToken()) {
                    doSetup(loginData, masterKey.getObject(), code.getObject());
                }
            }
        };
        add(form);
        form.add(new GoogleAuthSetupPanel("otp", loginData.getSubsystemTitle(), loginData.getUsername(), masterKey));
        form.add(new TextField<>("code", code));
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

    private void doSetup(SSOLoginData loginData, String secret, String code) {
        // the flag may be stale (e.g. another session has enrolled a key in the meantime)
        if (!loginData.isGoogleAuthSetupRequired()
                || StringUtils.hasLength(userService.getOtpMasterKeyByUsername(loginData.getUsername()))) {
            loginData.setGoogleAuthSetupRequired(false);
            throw new RestartResponseException(new SSOLoginErrorPage(new Model<>("No login data found")));
        }
        if (!isValidCode(secret, code)) {
            showError("One-time password value did not match.");
            return;
        }
        try {
            hotpService.persistOtpKey(OTPType.GOOGLE_AUTH, loginData.getUsername(), secret);
        } catch (SsoDecryptException e) {
            showError("Please try again");
            return;
        }
        loginData.setGoogleAuthSetupRequired(false);
        getRequestCycle().setResponsePage(new SSOLoginHOTPPage());
    }

    private static boolean isValidCode(String secret, String code) {
        if (code == null || !code.matches("^[0-9]{6}$")) {
            return false;
        }
        return new GoogleAuthenticator().authorize(secret, Integer.parseInt(code));
    }

    private void showError(String message) {
        errorMessageModel.setObject(message);
        errorMessageLabel.setVisible(true);
    }
}
