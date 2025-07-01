package com.payneteasy.superfly.api.serialization;

import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.exceptions.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for exception serialization and deserialization.
 */
public class ExceptionSerializationHelper {

    private static final Map<String, Class<? extends Throwable>> EXCEPTION_CLASSES = new HashMap<>();

    static {
        // Register exception classes that can be recreated on the client side
        registerExceptionClass(UserExistsException.class);
        registerExceptionClass(PolicyValidationException.class);
        registerExceptionClass(BadPublicKeyException.class);
        registerExceptionClass(MessageSendException.class);
        registerExceptionClass(UserNotFoundException.class);
        registerExceptionClass(SsoDecryptException.class);
        registerExceptionClass(SsoAuthException.class);
        registerExceptionClass(SsoUserException.class);
        registerExceptionClass(SsoSystemException.class);
        registerExceptionClass(SsoDataException.class);
    }

    /**
     * Registers an exception class for subsequent deserialization.
     *
     * @param exceptionClass exception class
     */
    private static void registerExceptionClass(Class<? extends Throwable> exceptionClass) {
        EXCEPTION_CLASSES.put(exceptionClass.getName(), exceptionClass);
    }

    /**
     * Creates an exception based on ExceptionWrapper.
     *
     * @param wrapper ExceptionWrapper object
     * @return exception recovered from the wrapper
     */
    public static Throwable createException(ExceptionWrapper wrapper) {
        String exceptionClassName = wrapper.getExceptionClass();
        Class<? extends Throwable> exceptionClass = EXCEPTION_CLASSES.get(exceptionClassName);

        if (exceptionClass == null) {
            // If the exception class is not registered, return a general exception
            return new SsoException(wrapper.getMessage() + " (" + exceptionClassName + ")") {
                // Anonymous subclass of SsoException
            };
        }

        try {
            // Try to create an instance of the exception using a constructor that accepts a message
            Constructor<? extends Throwable> constructor = exceptionClass.getConstructor(String.class);
            return constructor.newInstance(wrapper.getMessage());
        } catch (Exception e) {
            // In case of an error, create a general exception
            return new SsoException("Failed to deserialize exception: " + wrapper.getMessage()) {
                // Анонимный подкласс SsoException
            };
        }
    }
}
