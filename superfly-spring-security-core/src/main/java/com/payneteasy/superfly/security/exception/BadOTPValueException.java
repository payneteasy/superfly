package com.payneteasy.superfly.security.exception;

import com.payneteasy.superfly.api.CheckOtpResult;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Exception that is thrown when a one-time password check fails.
 *
 * @author Roman Puchkovskiy
 */
public class BadOTPValueException extends BadCredentialsException {
    private static final long serialVersionUID = 7345118080712359843L;

    /** Reason reported by the OTP check; null when the failure has no check result (e.g. no code given). */
    private final CheckOtpResult.Status status;

    public BadOTPValueException(String msg, CheckOtpResult.Status status) {
        super(msg);
        this.status = status;
    }

    public CheckOtpResult.Status getStatus() {
        return status;
    }

    public BadOTPValueException(String msg, Throwable t) {
        super(msg, t);
        this.status = null;
    }

    public BadOTPValueException(String msg) {
        super(msg);
        this.status = null;
    }

}
