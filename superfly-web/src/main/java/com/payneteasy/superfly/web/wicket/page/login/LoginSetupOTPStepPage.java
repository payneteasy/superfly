package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.security.SuperflyInitOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.wicket.component.otp.GoogleAuthSetupPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class LoginSetupOTPStepPage extends AbstractLoginPage {

    @SpringBean
    private UserService userService;

    public LoginSetupOTPStepPage() {
        if (!(getLatestReadyAuthentication() instanceof LocalNeedOTPToken localNeedOTPToken)) {
            throw new RestartResponseException(currentStepPage());
        }
        String relativePath = getRequest().getPrefixToContextPath();
        String url = buildSuperflyHOTPSecurityCheckUrl(relativePath);
        // shown only: the form sends the code, the key stays in the session
        String key = SuperflyInitOTPAuthenticationProcessingFilter.pendingKey(getHttpSession(),
                localNeedOTPToken.getName());

        Form<Void> form = new Form<Void>("form");
        form.add(new GoogleAuthSetupPanel("otp", "superfly", localNeedOTPToken.getName(), Model.of(key)));
        form.add(new AttributeModifier("action", Model.of(url)));
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
