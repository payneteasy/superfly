package com.payneteasy.superfly.security.processor;

import org.springframework.security.core.Authentication;

/**
 * Post-processes authentication.
 *
 * @author Roman Puchkovskiy
 */
public interface AuthenticationPostProcessor {
    /**
     * Post-processes authentication.
     *
     * @param auth    auth to process
     * @return processing result
     */
    public Authentication postProcess(Authentication auth);
}
