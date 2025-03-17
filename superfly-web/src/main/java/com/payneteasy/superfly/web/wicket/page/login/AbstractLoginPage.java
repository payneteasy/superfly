package com.payneteasy.superfly.web.wicket.page.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;

import com.payneteasy.superfly.security.exception.BadOTPValueException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

public abstract class AbstractLoginPage extends WebPage {
    protected String getSpringSecurityExceptionMessageAndRemoveException() {
        HttpSession session = getHttpSession();
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
            } else if (ex instanceof CsrfLoginTokenException) {
                result = ((CsrfLoginTokenException) ex).getPublicMsg();
            } else {
                result = "The username or password you entered is incorrect or user is locked.";
            }
        }
        return result;
    }

    @SpringBean
    protected CsrfValidator csrfValidator;

    protected Component createCsrfHiddenInput(String markupId) {
        String token = csrfValidator.persistTokenIntoSession(getHttpSession());
        return new HiddenField<>(markupId, Model.of(token)).add(new AttributeModifier("name", "_csrf"));
    }

    protected Authentication getLatestReadyAuthentication() {
        HttpSession session = getHttpSession();
        SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (context == null) {
            return null;
        }
        CompoundAuthentication compoundAuthentication = (CompoundAuthentication) context.getAuthentication();
        return compoundAuthentication != null ? compoundAuthentication.getLatestReadyAuthentication() : null;
    }

    private HttpSession getHttpSession() {
        ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
        HttpServletRequest request = servletWebRequest.getContainerRequest();

        return request.getSession(false);
    }

    protected void addMessage(Form<Void> form) {
        String message = getSpringSecurityExceptionMessageAndRemoveException();
        Label reasonLabel = new Label("reason", message);
        reasonLabel.setVisible(message != null);
        form.add(reasonLabel);
        add(form);
    }

}
