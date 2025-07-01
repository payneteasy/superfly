package com.payneteasy.superfly.api.exceptions;

/**
 * Thrown when a can not decrypt message
 *
 * @author Igor Vasilyev
 * @since 1.7
 */
public class SsoDecryptException extends SsoException {
    public SsoDecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public SsoDecryptException(String message) {
        super(message);
    }

}
