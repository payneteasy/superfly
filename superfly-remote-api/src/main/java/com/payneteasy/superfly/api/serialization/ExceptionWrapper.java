package com.payneteasy.superfly.api.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Exception wrapper for serialization.
 * Used to transfer exception information via HTTP.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionWrapper {
    private String exceptionClass;
    private String message;
    private String detailMessage;

    /**
     * Creates a wrapper from an exception
     *
     * @param exception exception to wrap
     * @return ExceptionWrapper object
     */
    public static ExceptionWrapper from(Throwable exception) {
        return new ExceptionWrapper(
            exception.getClass().getName(),
            exception.getMessage(),
            exception.toString()
        );
    }
}
