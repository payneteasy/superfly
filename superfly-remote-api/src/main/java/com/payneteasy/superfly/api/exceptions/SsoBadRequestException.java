package com.payneteasy.superfly.api.exceptions;

/**
 * HTTP 400 - Неверный запрос
 */
public class SsoBadRequestException extends SsoClientException {
    public SsoBadRequestException(String message) {
        super(400, message);
    }
}
