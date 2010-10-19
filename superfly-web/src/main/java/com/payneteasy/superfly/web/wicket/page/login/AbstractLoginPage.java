package com.payneteasy.superfly.web.wicket.page.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

import com.payneteasy.superfly.security.exception.BadOTPValueException;

public abstract class AbstractLoginPage extends WebPage {
    protected String getSpringSecurityExceptionMessageAndRemoveException() {
        ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
        HttpServletRequest request = servletWebRequest.getHttpServletRequest();

        HttpSession session = request.getSession(false);
        Object ex = null;
        if (session != null) {
        	ex = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        	if (ex != null) {
        		session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        	}
        }
        
        String result = null;
        if (ex != null) {
        	if (ex instanceof BadOTPValueException) {
        		result = "The one-time password value you entered is incorrect.";
        	} else {
        		result = "The username or password you entered is incorrect or user is locked.";
        	}
        }
		return result;
    }
    
	protected void addMessage(Form<Void> form) {
		String message = getSpringSecurityExceptionMessageAndRemoveException();
        Label reasonLabel = new Label("reason", message);
        reasonLabel.setVisible(message != null);
		form.add(reasonLabel);
		add(form);
	}

}
