package com.payneteasy.superfly.api.exceptions;

/**
 * Thrown if a message could not be sent.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
public class MessageSendException extends SsoClientException {
    public MessageSendException(String message) {
        super(500, message);
    }

}
