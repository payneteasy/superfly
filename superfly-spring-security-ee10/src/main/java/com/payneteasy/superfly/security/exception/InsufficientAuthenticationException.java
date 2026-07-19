package com.payneteasy.superfly.security.exception;

/**
 * This is a replacement for
 * {@link org.springframework.security.authentication.InsufficientAuthenticationException}
 * which cannot carry Authentication instances in recent
 * Spring Security versions.
 *
 * Note: this exception class carries an
 * {@link org.springframework.security.core.Authentication} instance,
 * but this is safe here.
 *
 * @author rpuch
 * @see com.payneteasy.superfly.security.InsufficientAuthenticationHandlingFilter
 * @see com.payneteasy.superfly.security.InsufficientAuthenticationAccessDecisionManager
 */
public class InsufficientAuthenticationException extends AuthenticationCarryingException {
    public InsufficientAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientAuthenticationException(String message) {
        super(message);
    }
}
