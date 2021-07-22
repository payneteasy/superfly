package com.payneteasy.superfly.crypto.exception;

public class DecryptException extends Exception {
    public DecryptException() {
        super();
    }

    public DecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecryptException(String message) {
        super(message);
    }

    public DecryptException(Throwable cause) {
        super(cause);
    }
}
