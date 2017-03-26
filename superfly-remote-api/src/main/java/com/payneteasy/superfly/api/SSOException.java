package com.payneteasy.superfly.api;

/**
 * Parent for all exceptions thrown by {@link SSOService}.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-2
 */
public class SSOException extends Exception {

    public SSOException() {
    }

    public SSOException(String message) {
        super(message);
    }

    public SSOException(Throwable cause) {
        super(cause);
    }

    public SSOException(String message, Throwable cause) {
        super(message, cause);
    }

}
