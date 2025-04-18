package com.payneteasy.superfly.api.exceptions;

/**
 * Ошибки парсинга ответа
 */
public class SsoParseException extends SsoException {
    public SsoParseException(String message) {
        super(message);
    }

    public SsoParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
