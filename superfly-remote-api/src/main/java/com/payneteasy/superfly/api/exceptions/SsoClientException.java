package com.payneteasy.superfly.api.exceptions;

import lombok.Getter;

/**
 * Базовое исключение для ошибок клиента (HTTP 4xx)
 */
@Getter
public class SsoClientException extends SsoException {
    private final int statusCode;

    public SsoClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
    public SsoClientException( String message) {
        super(message);
        this.statusCode = 0;
    }

}
