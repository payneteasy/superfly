package com.payneteasy.superfly.email;

public class RuntimeMessagingException extends RuntimeException {

	public RuntimeMessagingException() {
	}

	public RuntimeMessagingException(String message) {
		super(message);
	}

	public RuntimeMessagingException(Throwable cause) {
		super(cause);
	}

	public RuntimeMessagingException(String message, Throwable cause) {
		super(message, cause);
	}

}
