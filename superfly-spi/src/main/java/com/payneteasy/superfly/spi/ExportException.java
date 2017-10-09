package com.payneteasy.superfly.spi;

/**
 * Thrown when {@link HOTPProvider} cannot export its file due to
 * some internal reason not related to I/O problems.
 *
 * @author rpuch
 */
public class ExportException extends RuntimeException {
    public ExportException() {
        super();
    }

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportException(Throwable cause) {
        super(cause);
    }
}
