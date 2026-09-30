package com.payneteasy.superfly.security.processor;

import org.springframework.security.core.Authentication;

/**
 * AuthenticationPostProcessor implementation which just returns its argument.
 *
 * @author rpuch
 */
public class IdAuthenticationPostProcessor implements AuthenticationPostProcessor {
    @Override
    public Authentication postProcess(Authentication auth) {
        return auth;
    }
}
