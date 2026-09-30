package com.payneteasy.superfly.security.exception;

/**
 * Thrown to signal that step 2 of two-step authentication process
 * must be initiated.
 * Note: this exception class carries an
 * {@link org.springframework.security.core.Authentication} instance,
 * but this is safe here.
 *
 * @author Roman Puchkovskiy
 */
public class StepTwoException extends AuthenticationCarryingException {
    private static final long serialVersionUID = -5091470653099959408L;

    public StepTwoException(String msg) {
        super(msg);
    }

    public StepTwoException(String msg, Throwable t) {
        super(msg, t);
    }

}
