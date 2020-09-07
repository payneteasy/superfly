package com.payneteasy.superfly.notification;

/**
 * Thrown if a notification fails.
 * 
 * @author Roman Puchkovskiy
 */
public class NotificationException extends Exception {

    public NotificationException() {
    }

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(Throwable cause) {
        super(cause);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

}
