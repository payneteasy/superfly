package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки связанные с операциями над пользователем
 */
public class SsoUserException extends SsoException {
    public SsoUserException(String message) {
        super(message);
    }

    public SsoUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
