package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки работы с данными
 */
public class SsoDataException extends SsoException {
    public SsoDataException(String message) {
        super(message);
    }

    public SsoDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
