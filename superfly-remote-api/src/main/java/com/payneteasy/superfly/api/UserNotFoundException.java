package com.payneteasy.superfly.api;

import com.payneteasy.superfly.api.exceptions.SsoException;

/**
 * Thrown when a user is not found.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
public class UserNotFoundException extends SsoException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
