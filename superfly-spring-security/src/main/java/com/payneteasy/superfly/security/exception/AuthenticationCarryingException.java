package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * This is added because we need to transport Authentication
 * instance in our multi-step login flows.
 * In Spring Security (about version 4), Authentication
 * was removed from AuthenticationException to avoid data leakage.
 * We only use this for two classes: {@link StepTwoException} and
 * {@link InsufficientAuthenticationException} for which leakage
 * does not make any harm.
 *
 * @author rpuch
 */
abstract class AuthenticationCarryingException extends AuthenticationException {
    private Authentication authentication;

    protected AuthenticationCarryingException(String message, Throwable cause) {
        super(message, cause);
    }

    protected AuthenticationCarryingException(String message) {
        super(message);
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
}
