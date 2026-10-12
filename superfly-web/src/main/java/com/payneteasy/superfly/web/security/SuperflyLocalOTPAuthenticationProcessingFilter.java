package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.security.SuperflyOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for HOTP checking in Superfly webapp itself.
 *
 * @author Roman Puchkovskiy
 */
@Setter
public class SuperflyLocalOTPAuthenticationProcessingFilter extends
        SuperflyOTPAuthenticationProcessingFilter {
    private CsrfValidator csrfValidator;

    /**
     * This filter comes before the password and the TOTP setup filters, so after the request it sees the login state
     * that any of them has left.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            super.doFilter(request, response, chain);
        } finally {
            SuperflyInitOTPAuthenticationProcessingFilter.dropKeyUnlessSetupPending((HttpServletRequest) request);
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        csrfValidator.validateToken(request);
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String otp) {
        // e.g. the TOTP key is not set up yet: the code of that step goes to the setup form
        if (!(authentication instanceof UsernamePasswordAuthenticationToken token)) {
            throw new BadCredentialsException("OTP check is not pending");
        }
        return new LocalCheckOTPToken(authentication.getName(), otp, token.getAuthorities());
    }

}
