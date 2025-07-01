package com.payneteasy.superfly.common.session;

import java.util.Collection;

/**
 * Used to maintain a correlation between sessions and Superfly session
 * keys (or identifiers).
 *
 * @author Roman Puchkovskiy
 * @param <S> the session type
 */
public interface SessionMapping<S> {
    /**
     * Adds a session to the mapping.
     *
     * @param key        session key
     * @param session    session to add
     */
    void addSession(String key, S session);
    /**
     * Removes a session by its key.
     *
     * @param key    key by which to remove a session
     * @return removed session (or null if no such session existed in the mapping)
     */
    S removeSessionByKey(String key);
    /**
     * Removes a session by its ID.
     *
     * @param id ID of the session to remove
     */
    void removeSessionById(String id);
    /**
     * Removes all sessions from the mapping.
     *
     * @return removed sessions
     */
    Collection<S> clear();
}
