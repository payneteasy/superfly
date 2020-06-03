package com.payneteasy.superfly.security.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Exception that is thrown when a Google Auth key check fails.
 *
 * @author Igor Vasilyev
 */
public class BadGoogleAuthValueException extends BadCredentialsException {
    private static final long serialVersionUID = 1325148081712399873L;

    public BadGoogleAuthValueException(String msg, Throwable t) {
        super(msg, t);
    }

    public BadGoogleAuthValueException(String msg) {
        super(msg);
    }

}
