package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки работы с системой
 */
public class SsoSystemException extends SsoException {
    public SsoSystemException(String message) {
        super(message);
    }

    public SsoSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
