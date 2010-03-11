package com.payneteasy.superfly.client.session;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;

/**
 * Session listener that removes destroyed sessions from the session mapping.
 * 
 * @author Roman Puchkovskiy
 */
public class SessionMappingMaintainingListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent se) {
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		getSessionMapping().removeSessionById(se.getSession().getId());
	}

	protected SessionMapping getSessionMapping() {
		return SessionMappingLocator.getSessionMapping();
	}

}
