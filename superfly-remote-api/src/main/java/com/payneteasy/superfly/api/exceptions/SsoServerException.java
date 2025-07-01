package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки сервера (HTTP 5xx)
 */
public class SsoServerException extends SsoException {
    public SsoServerException(String message) {
        super(message);
    }

    public SsoServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
