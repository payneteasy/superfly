package com.payneteasy.superfly.api;

/**
 * Thrown when a user is not found.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
public class UserNotFoundException extends SSOException {

	public UserNotFoundException() {
	}

	public UserNotFoundException(String message) {
		super(message);
	}

	public UserNotFoundException(Throwable cause) {
		super(cause);
	}

	public UserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
