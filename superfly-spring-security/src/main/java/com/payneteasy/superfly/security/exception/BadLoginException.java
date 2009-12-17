package com.payneteasy.superfly.security.exception;

import org.springframework.security.AuthenticationException;

/**
 * Thrown to indicate a bad login.
 * 
 * @author Roman Puchkovskiy
 */
public class BadLoginException extends AuthenticationException {

	public BadLoginException(String message) {
		super(message);
	}

	public BadLoginException(String message, Throwable cause) {
		super(message, cause);
	}

}
