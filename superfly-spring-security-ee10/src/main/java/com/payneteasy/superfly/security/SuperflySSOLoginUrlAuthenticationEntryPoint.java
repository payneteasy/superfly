package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.client.SSOLoginState;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Entry point for the Superfly SSO login page: adds a one-time {@code state} parameter to the
 * login URL and keeps it in the HTTP session, so that
 * {@link SuperflySSOAuthenticationProcessingFilter} accepts a subsystem token only in the browser
 * session which started the login.
 * <p>
 * The login URL is the SSO login page with {@code subsystemIdentifier} and {@code targetUrl}
 * parameters; {@code state} is appended to it.
 */
public class SuperflySSOLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    public SuperflySSOLoginUrlAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     AuthenticationException exception) {
        String state = SSOLoginState.generate();
        request.getSession(true).setAttribute(SSOLoginState.SESSION_ATTRIBUTE, state);
        return SSOLoginState.appendTo(super.determineUrlToUseForThisRequest(request, response, exception), state);
    }
}
