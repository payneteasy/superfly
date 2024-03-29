package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.wicket.component.otp.GoogleAuthSetupPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class LoginSetupOTPStepPage extends AbstractLoginPage {

    @SpringBean
    private UserService userService;

    public LoginSetupOTPStepPage() {
        String relativePath = getRequest().getPrefixToContextPath();
        String url = buildSuperflyHOTPSecurityCheckUrl(relativePath);
        LocalNeedOTPToken localNeedOTPToken = (LocalNeedOTPToken) getLatestReadyAuthentication();

        if (localNeedOTPToken == null) {
            setResponsePage(LoginPasswordStepPage.class);
        }
        Form<Void> form = new Form<Void>("form") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(localNeedOTPToken != null);
                add(new GoogleAuthSetupPanel("otp", "superfly", localNeedOTPToken.getName(), Model.of("")));
                add(new AttributeModifier("action", Model.of(url)));
            }

            @Override
            protected void onSubmit() {
                super.onSubmit();
            }
        };

        form.add(createCsrfHiddenInput("_csrf"));

        addMessage(form);
    }

    protected String buildSuperflyHOTPSecurityCheckUrl(String relativePath) {
        String url;
        String postfix = "j_superfly_otp_reset";
        if ("".equals(relativePath)) {
            url = postfix;
        } else {
            url = relativePath + "/" + postfix;
        }
        return url;
    }

}
