package com.payneteasy.superfly.api.transport;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SLF4J provider of the tests of this module, registered in {@code META-INF/services}: the module has no logging
 * backend, so the provider keeps the log lines in memory for a test to read.
 */
public class RecordingSlf4jProvider implements SLF4JServiceProvider {

    /** A line as it was written: the level, the name of the logger and the message with its arguments filled in. */
    record Line(String level, String logger, String message) {
    }

    /** Lines of all loggers, in the order they were written. */
    static final List<Line> LINES = new CopyOnWriteArrayList<>();

    // not final: a constant would be folded by the compiler into the SLF4J API version the provider was built with
    @SuppressWarnings("FieldMayBeFinal")
    private static String requestedApiVersion = "2.0.99";

    private final IMarkerFactory markerFactory = new BasicMarkerFactory();
    private final MDCAdapter mdcAdapter = new NOPMDCAdapter();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return RecordingLogger::new;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return requestedApiVersion;
    }

    @Override
    public void initialize() {
    }

    private static final class RecordingLogger extends LegacyAbstractLogger {

        RecordingLogger(String name) {
            this.name = name;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        protected String getFullyQualifiedCallerName() {
            return null;
        }

        @Override
        protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern,
                Object[] arguments, Throwable throwable) {
            LINES.add(new Line(level.toString(), name, MessageFormatter.arrayFormat(messagePattern, arguments).getMessage()));
        }
    }
}
