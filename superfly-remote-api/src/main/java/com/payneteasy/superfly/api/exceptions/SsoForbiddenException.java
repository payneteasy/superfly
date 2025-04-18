package com.payneteasy.superfly.api.exceptions;

/**
 * HTTP 403 - Доступ запрещен
 */
public class SsoForbiddenException extends SsoClientException {
    public SsoForbiddenException(String message) {
        super(403, message);
    }
}
