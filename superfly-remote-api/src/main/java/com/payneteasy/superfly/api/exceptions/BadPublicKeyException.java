package com.payneteasy.superfly.api.exceptions;

/**
 * Thrown when a user's public key has bad format.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-2
 */
public class BadPublicKeyException extends SsoClientException {
    public BadPublicKeyException(String message) {
        super(400, message);
    }
}
