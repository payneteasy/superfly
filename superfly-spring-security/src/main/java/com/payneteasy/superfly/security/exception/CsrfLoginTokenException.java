package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.AuthenticationException;

public class CsrfLoginTokenException extends AuthenticationException {

    private String publicMsg;

    public CsrfLoginTokenException(String msg, Throwable t) {
        super(msg, t);
    }

    public CsrfLoginTokenException(String msg, String publicMsg) {
        super(msg);
        this.publicMsg = publicMsg;
    }

    public String getPublicMsg() {
        return publicMsg;
    }
}
