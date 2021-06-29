package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
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

    private IModel<String> errorMessageModel = new Model<String>();
    private Label errorMessageLabel;

    public SSOSetupGoogleAuthPage() {
        final SSOLoginData loginData = SSOUtils.getSsoLoginData(this);
        if (loginData == null) {
            RequestCycle.get().setResponsePage(new SSOLoginErrorPage(new Model<>("No login data found")));
        }

        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                doOnSubmit();
            }
        };
        add(form);

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
