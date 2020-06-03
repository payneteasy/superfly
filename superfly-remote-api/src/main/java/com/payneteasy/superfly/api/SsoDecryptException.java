package com.payneteasy.superfly.api;

/**
 * Thrown when a can not decrypt message
 *
 * @author Igor Vasilyev
 * @since 1.7
 */
public class SsoDecryptException extends SSOException {

    public SsoDecryptException() {
        super();
    }

    public SsoDecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public SsoDecryptException(String message) {
        super(message);
    }

    public SsoDecryptException(Throwable cause) {
        super(cause);
    }

}
