package com.payneteasy.superfly.api.serialization;

import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.exceptions.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExceptionSerializationHelperTest {

    @Test
    public void testCreateExceptionFromWrapper() {
        // Testing creation of different exception types
        testExceptionCreation(UserExistsException.class, "User already exists");
        testExceptionCreation(PolicyValidationException.class, "Password policy validation failed");
        testExceptionCreation(BadPublicKeyException.class, "Invalid public key");
        testExceptionCreation(MessageSendException.class, "Failed to send message");
        testExceptionCreation(UserNotFoundException.class, "User not found");
        testExceptionCreation(SsoDecryptException.class, "Failed to decrypt data");
        testExceptionCreation(SsoAuthException.class, "Authentication failed");
        testExceptionCreation(SsoUserException.class, "User operation failed");
        testExceptionCreation(SsoSystemException.class, "System error");
        testExceptionCreation(SsoDataException.class, "Data access error");
    }

    @Test
    public void testCreateUnregisteredExceptionFromWrapper() {
        // Testing creation of an unregistered exception
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.NonExistentException",
                "Unknown error",
                "Detailed error message"
        );

        Throwable exception = ExceptionSerializationHelper.createException(wrapper);

        // Check that an instance of SsoException is created
        assertTrue(exception instanceof SsoException);
        assertTrue(exception.getMessage().contains("Unknown error"));
        assertTrue(exception.getMessage().contains("NonExistentException"));
    }

    private <T extends Throwable> void testExceptionCreation(Class<T> exceptionClass, String message) {
        ExceptionWrapper wrapper = new ExceptionWrapper(
                exceptionClass.getName(),
                message,
                exceptionClass.getSimpleName() + ": " + message
        );

        Throwable exception = ExceptionSerializationHelper.createException(wrapper);

        // Check the type of the created exception
                        assertTrue("Expected type " + exceptionClass.getName() + ", got " + exception.getClass().getName(),
                exceptionClass.isInstance(exception));

        // Check the message
        assertEquals(message, exception.getMessage());
    }
}
