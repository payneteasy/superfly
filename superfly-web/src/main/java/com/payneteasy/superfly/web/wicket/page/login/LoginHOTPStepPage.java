package com.payneteasy.superfly.web.wicket.page.login;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

public class LoginHOTPStepPage extends AbstractLoginPage {
	
	public LoginHOTPStepPage() {
		String relativePath = getRequest().getPrefixToContextPath();
		String url = buildSuperflyHOTPSecurityCheckUrl(relativePath);
		Form<Void> form = new Form<Void>("form");
		form.add(new AttributeModifier("action", new Model<String>(url)));
		
		addMessage(form);
	}
	
    protected String buildSuperflyHOTPSecurityCheckUrl(String relativePath) {
        String url;
        String postfix = "j_superfly_hotp_security_check";
        if ("".equals(relativePath)) {
            url = postfix;
        } else {
            url = relativePath + "/" + postfix;
        }
        return url;
    }

}
