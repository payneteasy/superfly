package com.payneteasy.superfly.api;

/**
 * Thrown when a user's public key has bad format.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-2
 */
public class BadPublicKeyException extends SSOException {

    public BadPublicKeyException() {
        super();
    }

    public BadPublicKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadPublicKeyException(String message) {
        super(message);
    }

    public BadPublicKeyException(Throwable cause) {
        super(cause);
    }

}
