package com.payneteasy.superfly.api;

/**
 * Thrown if a message could not be sent.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
public class MessageSendException extends SSOException {

	public MessageSendException() {
		super();
	}

	public MessageSendException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageSendException(String message) {
		super(message);
	}

	public MessageSendException(Throwable cause) {
		super(cause);
	}

}
