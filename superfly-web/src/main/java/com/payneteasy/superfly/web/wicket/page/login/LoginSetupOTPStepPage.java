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

    private final String username;

    public LoginSetupOTPStepPage() {
        if (!(getLatestReadyAuthentication() instanceof LocalNeedOTPToken localNeedOTPToken)) {
            throw new RestartResponseException(currentStepPage());
        }
        username = localNeedOTPToken.getName();
        SuperflyInitOTPAuthenticationProcessingFilter.pendingKey(getHttpSession(), username);
        String relativePath = getRequest().getPrefixToContextPath();
        String url = buildSuperflyHOTPSecurityCheckUrl(relativePath);

        Form<Void> form = new Form<Void>("form");
        // shown only, and read from the session on every render: the form sends the code, and a stored instance of
        // this page shows nothing once the setup is over
        form.add(new GoogleAuthSetupPanel("otp", "superfly", username, this::issuedKey));
        form.add(new AttributeModifier("action", Model.of(url)));
        form.add(createCsrfHiddenInput("_csrf"));

        addMessage(form);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        if (issuedKey() == null) {
            throw new RestartResponseException(currentStepPage());
        }
    }

    /** @return the key of the setup in progress, or null if the login is not at the setup step of this user */
    private String issuedKey() {
        if (getLatestReadyAuthentication() instanceof LocalNeedOTPToken setup && setup.getName().equals(username)) {
            return SuperflyInitOTPAuthenticationProcessingFilter.issuedKey(getHttpSession(), username);
        }
        return null;
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
