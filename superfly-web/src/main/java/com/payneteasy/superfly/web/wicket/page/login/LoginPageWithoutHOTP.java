package com.payneteasy.superfly.web.wicket.page.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

public class LoginPageWithoutHOTP extends WebPage {
    public LoginPageWithoutHOTP() {
        String relativePath = getRequest().getPrefixToContextPath();
        String url = buildSuperflyPasswordSecurityCheckUrl(relativePath);
        Form<Void> form = new Form<Void>("form");
        form.add(new AttributeModifier("action", new Model<String>(url)));

        addMessage(form);
    }

    protected String buildSuperflyPasswordSecurityCheckUrl(String relativePath) {
        String url;
        String postfix = "j_spring_security_check";
        if ("".equals(relativePath)) {
            url = postfix;
        } else {
            url = relativePath + "/" + postfix;
        }
        return url;
    }

    protected String getSpringSecurityExceptionMessage() {
        ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
        HttpServletRequest request = servletWebRequest.getContainerRequest();

        HttpSession session = request.getSession(false);
        Object ex = null;
        if (session != null) {
            ex = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }

        String result = null;
        if (ex != null) {
            result = "The username or password you entered is incorrect or user is locked.";
        }
        return result;
    }

    protected void addMessage(Form<Void> form) {
        String message = getSpringSecurityExceptionMessage();
        Label reasonLabel = new Label("reason", message);
        reasonLabel.setVisible(message != null);
        form.add(reasonLabel);
        add(form);
    }
}
