package com.payneteasy.superfly.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

/**
 * Filter which authenticates a user using the Superfly password authentication.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyUsernamePasswordAuthenticationProcessingFilter extends
        AbstractSingleStepAuthenticationProcessingFilter {

    private String usernameParameter = "j_username";
    private String passwordParameter = "j_password";
    private String secondFactoryParameter = "j_second";
    private String subsystemIdentifier;
    private CsrfValidator csrfValidator;

    @Required
    public void setCsrfValidator(CsrfValidator csrfValidator) {
        this.csrfValidator = csrfValidator;
    }

    public SuperflyUsernamePasswordAuthenticationProcessingFilter() {
        super("/j_superfly_password_security_check");
    }

    public void setUsernameParameter(String usernameParameter) {
        this.usernameParameter = usernameParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        this.passwordParameter = passwordParameter;
    }

    public void setSubsystemIdentifier(String subsystemIdentifier) {
        this.subsystemIdentifier = subsystemIdentifier;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        csrfValidator.validateToken(request);

        Authentication authRequest;
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        String secondFactory = obtainSecondFactory(request);

        authRequest = createUsernamePasswordAuthRequest(request, username, password, secondFactory);

        return getAuthenticationManager().authenticate(new CompoundAuthentication(authRequest));
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(usernameParameter);
    }

    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(passwordParameter);
    }
    protected String obtainSecondFactory(HttpServletRequest request) {
        return request.getParameter(secondFactoryParameter);
    }

    protected Authentication createUsernamePasswordAuthRequest(
            HttpServletRequest request, String username, String password, String secondFactory) {
        AuthenticationRequestInfo authRequestInfo = createAuthRequestInfo(request);
        Authentication authRequest = new UsernamePasswordAuthRequestInfoAuthenticationToken(
                username, password, authRequestInfo).withSecondFactory(secondFactory);
        return authRequest;
    }

    protected AuthenticationRequestInfo createAuthRequestInfo(HttpServletRequest request) {
        AuthenticationRequestInfo result = new AuthenticationRequestInfo();
        result.setIpAddress(request.getRemoteAddr());
        result.setSubsystemIdentifier(subsystemIdentifier);
        return result;
    }

}
