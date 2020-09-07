package com.payneteasy.superfly.security;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.payneteasy.superfly.security.exception.AuthenticationCarrier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;

/**
 * AuthenticationEntryPoint implementation for two-step authentication process.
 * 
 * @author Roman Puchkovskiy
 */
public class TwoStepAuthenticationProcessingFilterEntryPoint extends
        LoginUrlAuthenticationEntryPoint {

    private final String loginFormStepTwoUrl;

    public TwoStepAuthenticationProcessingFilterEntryPoint(String loginFormUrl, String loginFormStepTwoUrl) {
        super(loginFormUrl);
        this.loginFormStepTwoUrl = loginFormStepTwoUrl;
    }

    public String getLoginFormStepTwoUrl() {
        return loginFormStepTwoUrl;
    }

    @Override
    protected String determineUrlToUseForThisRequest(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
        if (exception instanceof StepTwoException) {
            prepareForStepTwo(request, exception);
            return getLoginFormStepTwoUrl();
        } else {
            return getLoginFormUrl();
        }
    }

    protected void prepareForStepTwo(HttpServletRequest request,
            AuthenticationException reason) {
        Authentication authentication = null;
        if (reason instanceof AuthenticationCarrier) {
            authentication = ((AuthenticationCarrier) reason).getAuthentication();
        }
        if (authentication == null) {
            throw new IllegalStateException("No authentication could be extracted from exception, something is wrong");
        }
        SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) authentication;
        Set<SSORole> roles = token.getSsoUser().getActionsMap().keySet();
        request.getSession().setAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY, token.getSsoUser());
        if (!isServerSideRedirect()) {
            request.getSession().setAttribute("superflyRoles", roles);
            request.getSession().setAttribute("ctxPath", request.getContextPath());
        } else {
            request.setAttribute("superflyRoles", roles);
            request.setAttribute("ctxPath", request.getContextPath());
        }
    }

    /**
     * Defined as this method has disappeared from the superclass.
     *
     * @return true if redirect is server-side
     */
    protected boolean isServerSideRedirect() {
        return false;
    }

}
