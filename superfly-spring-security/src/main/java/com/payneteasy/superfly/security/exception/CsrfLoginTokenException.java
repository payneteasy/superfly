package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.AuthenticationException;

public class CsrfLoginTokenException extends AuthenticationException {

    public CsrfLoginTokenException(String msg, Throwable t) {
        super(msg, t);
    }

    public CsrfLoginTokenException(String msg) {
        super(msg);
    }
}
