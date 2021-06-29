package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.web.wicket.component.otp.GoogleAuthSetupPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
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

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOSetupGoogleAuthPage() {
        final SSOLoginData loginData = SSOUtils.getSsoLoginData(this);
        if (loginData == null || loginData.getUsername() == null) {
            RequestCycle.get().setResponsePage(new SSOLoginErrorPage(new Model<>("No login data found")));
        }

        IModel<String> masterKey = Model.of("");
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                try {
                    hotpService.persistOtpKey(OTPType.GOOGLE_AUTH, loginData.getUsername(), masterKey.getObject());
                } catch (SsoDecryptException e) {
                    error("Please try again");
                }
                doOnSubmit();
            }
        };
        add(form);
        form.add(new GoogleAuthSetupPanel("otp", loginData.getSubsystemTitle(), loginData.getUsername(), masterKey));

        errorMessageLabel = new Label("message", errorMessageModel);
        errorMessageLabel.setVisible(StringUtils.hasLength(errorMessageModel.getObject()));
        form.add(errorMessageLabel);
    }

    @Override
    protected IModel<String> createCustomCssUrlModel() {
        return new SubsystemLoginCssUrlModel(
                subsystemService, getSession().getSsoLoginData());
    }

    private void doOnSubmit() {

        getRequestCycle().setResponsePage(new SSOLoginHOTPPage());
    }
}
