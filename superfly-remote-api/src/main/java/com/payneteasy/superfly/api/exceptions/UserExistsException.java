package com.payneteasy.superfly.api.exceptions;

/**
 *
 *
 * @since 1.1
 */
public class UserExistsException extends SsoClientException {
    public UserExistsException(String message) {
        super(409, message);
    }
}
