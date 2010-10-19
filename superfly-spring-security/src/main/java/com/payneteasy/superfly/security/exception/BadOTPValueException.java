package com.payneteasy.superfly.security.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * Exception that is thrown when a one-time password check fails.
 *
 * @author Roman Puchkovskiy
 */
public class BadOTPValueException extends BadCredentialsException {
	private static final long serialVersionUID = 7345118080712359843L;

	public BadOTPValueException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}

	public BadOTPValueException(String msg, Throwable t) {
		super(msg, t);
	}

	public BadOTPValueException(String msg) {
		super(msg);
	}

}
