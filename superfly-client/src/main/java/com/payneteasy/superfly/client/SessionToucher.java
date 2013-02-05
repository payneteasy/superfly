package com.payneteasy.superfly.client;

/**
 * Used to touch sessions.
 *
 * @author Roman Puchkovskiy
 */
public interface SessionToucher {
    /**
     * Adds a session ID to be touched.
     *
     * @param sessionId ID of a session to touch
     */
    void addSessionId(long sessionId);
}
