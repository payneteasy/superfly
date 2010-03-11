package com.payneteasy.superfly.common.session;

import java.util.Collection;

import javax.servlet.http.HttpSession;

/**
 * Used to maintain a correlation between HttpSession's and Superfly session
 * keys (or identifiers).
 * 
 * @author Roman Puchkovskiy
 */
public interface SessionMapping {
	/**
	 * Adds a session to the mapping.
	 * 
	 * @param key		session key
	 * @param session	session to add
	 */
	void addSession(String key, HttpSession session);
	/**
	 * Removes a session by its key.
	 * 
	 * @param key	key by which to remove a session
	 * @return removed session (or null if no such session existed in the mapping)
	 */
	HttpSession removeSessionByKey(String key);
	/**
	 * Removes a session by its ID (i.e. by value returned by session.getId()).
	 * 
	 * @param id	ID of the session to remove
	 * @return removed session (or null if no such session existed in the mapping)
	 */
	HttpSession removeSessionById(String id);
	/**
	 * Removes all sessions from the mapping.
	 * 
	 * @return removed sessions
	 */
	Collection<HttpSession> clear();
}
