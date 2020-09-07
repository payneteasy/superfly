package com.payneteasy.superfly.client.exception;

/**
 * Thrown when a problem occurs during collection.
 * 
 * @author Roman Puchkovskiy
 */
public class CollectionException extends Exception {

    public CollectionException() {
    }

    public CollectionException(String message) {
        super(message);
    }

    public CollectionException(Throwable cause) {
        super(cause);
    }

    public CollectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
