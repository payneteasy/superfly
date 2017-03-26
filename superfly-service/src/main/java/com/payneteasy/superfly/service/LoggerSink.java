package com.payneteasy.superfly.service;

import org.slf4j.Logger;

/**
 * Used to aggregate and format log messages.
 * 
 * @author Roman Puchkovskiy
 */
public interface LoggerSink {
    /**
     * Logs using INFO level.
     *
     * @param logger                logger to which to log
     * @param eventType                type of the event
     * @param success                success or failure indication
     * @param resourceIdentity        identity of a resource
     */
    void info(Logger logger, String eventType, boolean success, String resourceIdentity);
}
