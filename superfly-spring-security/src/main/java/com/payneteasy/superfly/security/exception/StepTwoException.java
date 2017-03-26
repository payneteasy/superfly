package com.payneteasy.superfly.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown to signal that step 2 of two-step authentication process
 * must be initiated.
 * 
 * @author Roman Puchkovskiy
 * @see TwoStepAuthenticationProcessingFilter
 */
public class StepTwoException extends AuthenticationException {
    private static final long serialVersionUID = -5091470653099959408L;

    public StepTwoException(String msg) {
        super(msg);
    }

    public StepTwoException(String msg, Throwable t) {
        super(msg, t);
    }

    public StepTwoException(String msg, Object extraInformation) {
        super(msg, extraInformation);
    }

}
