package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.security.SuperflyOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter for HOTP checking in Superfly webapp itself.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflyLocalOTPAuthenticationProcessingFilter extends
        SuperflyOTPAuthenticationProcessingFilter {
    private CsrfValidator csrfValidator;

    @Required
    public void setCsrfValidator(CsrfValidator csrfValidator) {
        this.csrfValidator = csrfValidator;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        csrfValidator.validateToken(request);
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String otp) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        return new LocalCheckOTPToken(authentication.getName(), otp, token.getAuthorities());
    }

}
