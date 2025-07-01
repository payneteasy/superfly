package com.payneteasy.superfly.client.session;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import com.payneteasy.superfly.common.session.HttpSessionWrapper;
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

    protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
        return SessionMappingLocator.getSessionMapping();
    }

}
