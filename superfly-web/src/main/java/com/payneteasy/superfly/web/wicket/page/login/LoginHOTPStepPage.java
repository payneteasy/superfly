package com.payneteasy.superfly.web.wicket.page.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

public class LoginHOTPStepPage extends WebPage {
	
	public LoginHOTPStepPage() {
		String relativePath = getRequest().getRelativePathPrefixToContextRoot();
		String url = buildSuperflyHOTPSecurityCheckUrl(relativePath);
		Form<Void> form = new Form<Void>("form");
		form.add(new AttributeModifier("action", new Model<String>(url)));
        form.add(new WebMarkupContainer("reason").setVisible(hasSpringSecurityException()));
		add(form);
	}
	
    private boolean hasSpringSecurityException() {
        ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
        HttpServletRequest request = servletWebRequest.getHttpServletRequest();

        HttpSession session = request.getSession();
        return session!=null && session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION")!=null;
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
