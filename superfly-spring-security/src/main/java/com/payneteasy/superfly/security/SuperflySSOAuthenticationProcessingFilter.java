package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter which authenticates a user using the Superfly Single Sign-on
 * authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflySSOAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private String subsystemTokenParameter = "subsystemToken";
    private String targetUrlParameter = "targetUrl";

    public void setSubsystemTokenParameter(String subsystemTokenParameter) {
        this.subsystemTokenParameter = subsystemTokenParameter;
    }

    public void setTargetUrlParameter(String targetUrlParameter) {
        this.targetUrlParameter = targetUrlParameter;
    }

    public SuperflySSOAuthenticationProcessingFilter() {
        super("/j_superfly_sso_security_check");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        Authentication authRequest;

        String subsystemToken = obtainSubsystemToken(request);
        String targetUrl = obtainTargetUrl(request);

        authRequest = createSSOAuthRequest(request, subsystemToken);

        return getAuthenticationManager().authenticate(new CompoundAuthentication(authRequest));
    }

    protected String obtainSubsystemToken(HttpServletRequest request) {
        return request.getParameter(subsystemTokenParameter);
    }

    protected String obtainTargetUrl(HttpServletRequest request) {
        return request.getParameter(targetUrlParameter);
    }

    protected Authentication createSSOAuthRequest(
            HttpServletRequest request, String subsystemToken) {
        return new SSOAuthenticationRequest(subsystemToken);
    }

}
