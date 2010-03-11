package com.payneteasy.superfly.client.session;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;


/**
 * Contains some code useful when dealing with sessions.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSessionStoreAwareNotificationSinkFilter extends
		AbstractNotificationSinkFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractSessionStoreAwareNotificationSinkFilter.class);

	protected SessionMapping getSessionMapping() {
		return SessionMappingLocator.getSessionMapping();
	}

	protected void invalidateSessionQuietly(HttpSession session) {
		if (session != null) {
			try {
				session.invalidate();
			} catch (IllegalStateException e) {
				logger.warn("Ignored exception while trying to invalidate a session", e);
			}
		}
	}

}
