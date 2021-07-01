package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.wicket.page.BasePage;
import com.payneteasy.superfly.web.wicket.page.sso.SSOLoginErrorPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Page used to change a password.
 */
@Secured("ROLE_ACTION_TEMP_PASSWORD")
public class ChangePasswordPage extends BasePage {

    @SpringBean
    protected CsrfValidator csrfValidator;

    public ChangePasswordPage(PageParameters params) {
        super(ListUsersPage.class, params);

        add(new ChangePasswordPanel("change-password-panel") {
            @Override
            protected Component createCsrfInput(String aMarkupId) {
                return createCsrfHiddenInput(aMarkupId);
            }

            @Override
            protected boolean validateCsrf() {
                return validateCsrfToken();
            }

            @Override
            protected String getCurrentUserName() {
                return SecurityUtils.getUsername();
            }

            @Override
            protected void onPasswordChanged() {
                getRequestCycle().setResponsePage(getApplication().getHomePage());
                WebSession.get().invalidate();
                SecurityContextHolder.clearContext();
            }
        });
    }

    @Override
    protected String getTitle() {
        return "";
    }

    protected Component createCsrfHiddenInput(String markupId) {
        String token = csrfValidator.persistTokenIntoSession(getHttpSession());
        return new HiddenField<>(markupId, Model.of(token)).add(new AttributeModifier("name", "_csrf"));
    }

    protected boolean validateCsrfToken() {
        try {
            csrfValidator.validateToken(getHttpServletRequest());
            return true;
        } catch (Exception e) {
            if (e instanceof CsrfLoginTokenException) {
                error(((CsrfLoginTokenException)e).getPublicMsg());
            } else {
                error("Please refresh page and try again");
            }
            return false;
        }
    }

    private HttpSession getHttpSession() {
        HttpServletRequest request = getHttpServletRequest();
        return request.getSession(false);
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
        return servletWebRequest.getContainerRequest();
    }

}