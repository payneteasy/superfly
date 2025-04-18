package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки аутентификации/авторизации
 */
public class SsoAuthException extends SsoException {
    public SsoAuthException(String message) {
        super(message);
    }

    public SsoAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
