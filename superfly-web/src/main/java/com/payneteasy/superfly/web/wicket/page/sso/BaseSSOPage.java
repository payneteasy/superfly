package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.exception.CsrfLoginTokenException;
import com.payneteasy.superfly.web.wicket.page.SessionAccessorPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author rpuch
 */
public abstract class BaseSSOPage extends SessionAccessorPage {

    @SpringBean
    protected CsrfValidator csrfValidator;

    protected BaseSSOPage() {
        SSOLoginData loginData = getSession().getSsoLoginData();
        WebMarkupContainer subsystemInfoContainer = new WebMarkupContainer("subsystem-info");
        boolean subsystemInfoVisible = isSubsystemInfoShown() && loginData != null;
        subsystemInfoContainer.setVisible(subsystemInfoVisible);
        add(subsystemInfoContainer);

        if (subsystemInfoVisible) {
            subsystemInfoContainer.add(new Label("subsystem-title", loginData.getSubsystemTitle()));
            subsystemInfoContainer.add(new Label("subsystem-url", loginData.getSubsystemUrl()));
        }

        IModel<String> cssUrlModel = createCssUrlModel();
        WebMarkupContainer cssUrlContainer = new WebMarkupContainer("login-css-url");
        cssUrlContainer.add(new AttributeModifier("href", cssUrlModel));
        cssUrlContainer.setVisible(StringUtils.hasText(cssUrlModel.getObject()));
        add(cssUrlContainer);
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
            getRequestCycle().setResponsePage(getApplication().getHomePage());
            WebSession.get().invalidate();
            SecurityContextHolder.clearContext();
            if (e instanceof CsrfLoginTokenException) {
                setResponsePage(new SSOLoginErrorPage(new Model<>(((CsrfLoginTokenException)e).getPublicMsg())));
            } else {
                setResponsePage(new SSOLoginErrorPage(new Model<>("No login data found")));
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

    private IModel<String> createCssUrlModel() {
        IModel<String> customModel = createCustomCssUrlModel();
        if (StringUtils.hasText(customModel.getObject())) {
            return customModel;
        } else {
            ServletWebRequest request = (ServletWebRequest) getRequest();
            return new Model<String>(request.getContextPath() + "/css/sso-login-form.css");
        }
    }

    protected IModel<String> createCustomCssUrlModel() {
        return new Model<String>(null);
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");

        //for IE
        response.setHeader("Expires", "-1");
        response.setHeader("Pragma", "no-cache");
    }

    protected boolean isSubsystemInfoShown() {
        return true;
    }
}
