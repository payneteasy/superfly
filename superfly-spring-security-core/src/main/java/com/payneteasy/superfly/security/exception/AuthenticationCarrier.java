package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.Authentication;

/**
 * Carries {@link Authentication}.
 *
 * @author rpuch
 */
public interface AuthenticationCarrier {
    Authentication getAuthentication();
}
