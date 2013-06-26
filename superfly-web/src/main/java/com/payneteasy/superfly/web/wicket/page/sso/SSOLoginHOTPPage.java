package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
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
public class SSOLoginHOTPPage extends BaseSSOPage {
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
        if (loginData == null) {
            RequestCycle.get().setResponsePage(new SSOLoginErrorPage(new Model<String>("No login data found")));
        }

        final LoginBean loginBean = new LoginBean();
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                doOnSubmit(loginBean, loginData);
            }
        };
        add(form);
        form.add(new PasswordTextField("hotp", new PropertyModel<String>(loginBean, "hotp")));

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
        boolean ok = internalSSOService.authenticateHOTP(
                loginData.getSubsystemIdentifier(),
                loginData.getUsername(), loginBean.getHotp());
        if (ok) {
            onHOTPChecked(loginData);
        } else {
            errorMessageModel.setObject("One-time password value did not match.");
            errorMessageLabel.setVisible(true);
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
