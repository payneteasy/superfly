package com.payneteasy.superfly.api.exceptions;

/**
 * HTTP 404 - Ресурс не найден
 */
public class SsoNotFoundException extends SsoClientException {
    public SsoNotFoundException(String message) {
        super(404, message);
    }
}
