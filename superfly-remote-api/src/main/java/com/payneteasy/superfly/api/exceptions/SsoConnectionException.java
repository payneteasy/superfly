package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки соединения с SSO сервером
 */
public class SsoConnectionException extends SsoException {
    public SsoConnectionException(String message) {
        super(message);
    }

    public SsoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
