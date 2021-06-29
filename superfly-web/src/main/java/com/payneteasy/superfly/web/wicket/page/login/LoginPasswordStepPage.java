package com.payneteasy.superfly.web.wicket.page.login;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

public class LoginPasswordStepPage extends AbstractLoginPage {

    public LoginPasswordStepPage() {
        String relativePath = getRequest().getPrefixToContextPath();
        String url = buildSuperflyPasswordSecurityCheckUrl(relativePath);
        Form<Void> form = new Form<Void>("form");
        form.add(new AttributeModifier("action", new Model<String>(url)));
        form.add(createCsrfHiddenInput("_csrf"));
        addMessage(form);
    }

    protected String buildSuperflyPasswordSecurityCheckUrl(String relativePath) {
        String url;
        String postfix = "j_superfly_password_security_check";
        if ("".equals(relativePath)) {
            url = postfix;
        } else {
            url = relativePath + "/" + postfix;
        }
        return url;
    }

}
