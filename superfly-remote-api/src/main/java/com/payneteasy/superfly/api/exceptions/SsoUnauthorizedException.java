package com.payneteasy.superfly.api.exceptions;

/**
 * HTTP 401 - Не авторизован
 */
public class SsoUnauthorizedException extends SsoClientException {
    public SsoUnauthorizedException(String message) {
        super(401, message);
    }
}
