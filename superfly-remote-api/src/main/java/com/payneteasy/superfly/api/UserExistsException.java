package com.payneteasy.superfly.api;

/**
 * 
 *
 * @since 1.1
 */
public class UserExistsException extends SSOException {
	public UserExistsException() {
	}

	public UserExistsException(String message) {
		super(message);
	}

	public UserExistsException(Throwable cause) {
		super(cause);
	}

	public UserExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
