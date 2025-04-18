package com.payneteasy.superfly.api.exceptions;

/**
 * HTTP 409 - Конфликт данных
 */
public class SsoConflictException extends SsoClientException {
    public SsoConflictException(String message) {
        super(409, message);
    }
}
