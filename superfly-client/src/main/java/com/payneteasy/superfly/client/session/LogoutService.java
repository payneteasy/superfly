package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;
import com.payneteasy.superfly.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible exclusively for the logic of terminating Superfly sessions.
 * It knows nothing about servlets and HTTP, which makes it easily testable
 * and reusable.
 */
public class LogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);

    /**
     * Constant for the parameter name with session identifiers for logout.
     * Defined as a constant for unified usage between classes.
     */
    public static final String LOGOUT_SESSION_IDS_PARAM = "superflyLogoutSessionIds";

    /**
     * Handles user logout based on their session IDs.
     *
     * @param logoutSessionIds string with comma-separated session IDs.
     * @return true if logout was performed, false otherwise
     */
    public boolean handleLogout(String logoutSessionIds) {
        if (logoutSessionIds == null || logoutSessionIds.trim().isEmpty()) {
            logger.debug("Session IDs parameter for logout is empty. Nothing to do.");
            return false;
        }

        String[] sessionIds = StringUtils.commaDelimitedListToStringArray(logoutSessionIds);
        SessionMapping<HttpSessionWrapper> sessionMapping = getSessionMapping();

        for (String key : sessionIds) {
            HttpSessionWrapper session = sessionMapping.removeSessionByKey(key);
            invalidateSessionQuietly(session, key);
        }

        return true;
    }

    private void invalidateSessionQuietly(HttpSessionWrapper session, String key) {
        if (session != null) {
            try {
                session.invalidate();
                logger.info("Session for key '{}' successfully terminated.", key);
            } catch (IllegalStateException e) {
                // This situation is normal if the session has already been terminated.
                // Just log at warn level to avoid cluttering the logs.
                logger.warn("Attempt to terminate an already invalid session for key '{}'.", key, e);
            }
        } else {
            logger.warn("Session for key '{}' not found. It may have been terminated earlier.", key);
        }
    }

    protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
        return SessionMappingLocator.getSessionMapping();
    }
}
