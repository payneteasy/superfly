package com.payneteasy.superfly.api.exceptions;

/**
 * Базовое исключение для всех ошибок SSO
 */
public abstract class SsoException extends RuntimeException {
    public SsoException(String message) {
        super(message);
    }

    public SsoException(String message, Throwable cause) {
        super(message, cause);
    }
}
