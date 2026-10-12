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
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    /** Issued when the form is first rendered, not when the page is constructed. */
    private String csrfToken;

    protected Component createCsrfHiddenInput(String markupId) {
        return new HiddenField<String>(markupId, new Model<String>()) {
            @Override
            protected void onBeforeRender() {
                // a form component shows the value it was submitted with, which may not be the token of the session
                clearInput();
                setModelObject(currentCsrfToken());
                super.onBeforeRender();
            }
        }.add(new AttributeModifier("name", "_csrf"));
    }

    private String currentCsrfToken() {
        if (csrfToken == null) {
            csrfToken = csrfValidator.persistTokenIntoSession(getHttpSession());
        }
        return csrfToken;
    }

    /**
     * @return the last passed step of the login in progress, or null if there is none (no session, or the login is
     * complete)
     */
    protected Authentication getLatestReadyAuthentication() {
        HttpSession session = getHttpSession();
        if (session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") instanceof SecurityContext context
                && context.getAuthentication() instanceof CompoundAuthentication compoundAuthentication) {
            return compoundAuthentication.getLatestReadyAuthentication();
        }
        return null;
    }

    /** The page of the step that the login in progress is at, as the authentication entry point chooses it. */
    protected Class<? extends AbstractLoginPage> currentStepPage() {
        Authentication latest = getLatestReadyAuthentication();
        if (latest instanceof LocalNeedOTPToken) {
            return LoginSetupOTPStepPage.class;
        }
        if (latest instanceof UsernamePasswordAuthenticationToken) {
            return LoginOTPStepPage.class;
        }
        return LoginPasswordStepPage.class;
    }

    protected HttpSession getHttpSession() {
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
