package com.payneteasy.superfly.service;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.session.UISession;

import java.util.Date;
import java.util.List;

/**
 * Service for sessions.
 * 
 * @author Roman Puchkovskiy
 */
public interface SessionService {
	/**
	 * Returns a list of all invalid sessions (these are sessions which
	 * have not yet expired themselves but whose actions have expired).
	 * 
	 * @return invalid sessions
	 */
	List<UISession> getInvalidSessions();

	/**
	 * Returns a list of all expired sessions.
	 * 
	 * @return expired sessions
	 */
	List<UISession> getExpiredSessions();
	
	/**
	 * Makes invalid sessions expired.
	 * 
	 * @return routine result
	 */
	RoutineResult expireInvalidSessions();
	
	/**
	 * Deletes expired sessions. 'Expired' sessions are those which were
	 * expired explicitly.
	 * 
	 * After sessions have been deleted, their corresponding subsystems are
	 * notified that these sessions have been logged out.
	 * 
	 * @return deleted sessions
	 */
	List<UISession> deleteExpiredSessionsAndNotify();
	
	/**
	 * Deletes expired sessions. 'Expired' sessions are those for which any
	 * of the following conditions is true:
	 * 1. session was expired explicitly
	 * 2. session start date is before the given date
	 * If the specified date is null, only item 1 applies.
	 * 
	 * After sessions have been deleted, their corresponding subsystems are
	 * notified that these sessions have been logged out.
	 * 
	 * @param beforeWhat	date before which all sessions are considered as
	 * 						expired (ignored if null)
	 * @return deleted sessions
	 */
	List<UISession> deleteExpiredSessionsAndNotify(Date beforeWhat);
	
	/**
	 * Deletes expired sessions. 'Expired' sessions are those for which any
	 * of the following conditions is true:
	 * 1. session was expired explicitly
	 * 2. session start date is before current date minus the given number of
	 * sedonds
	 * 
	 * After sessions have been deleted, their corresponding subsystems are
	 * notified that these sessions have been logged out.
	 * 
	 * @param seconds	minimum session age for it to be considered as 'old'
	 * @return deleted sessions
	 */
	List<UISession> deleteExpiredAndOldSessionsAndNotify(int seconds);

    /**
     * Obtains a valid SSO session by its identifier if such session exists,
     * otherwise returns null.
     *
     * @param ssoSessionIdentifier  identifier of an SSO session
     * @return session or null
     */
    SSOSession getValidSSOSession(String ssoSessionIdentifier);
}
