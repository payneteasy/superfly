package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a precondition fails (for instance, the order of authentication
 * actions is wrong).
 *
 * @author Roman Puchkovskiy
 */
public class PreconditionsException extends AuthenticationException {
    private static final long serialVersionUID = 7098950681593983707L;

    public PreconditionsException(String msg, Throwable t) {
        super(msg, t);
    }

    public PreconditionsException(String msg) {
        super(msg);
    }

}
