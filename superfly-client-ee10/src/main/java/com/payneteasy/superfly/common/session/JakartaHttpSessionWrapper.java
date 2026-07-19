package com.payneteasy.superfly.common.session;

import jakarta.servlet.http.HttpSession;

/**
 * Implementation of HttpSessionWrapper for Jakarta Servlet API.
 *
 * @author Developer
 */
public class JakartaHttpSessionWrapper implements HttpSessionWrapper {

    private final HttpSession session;

    public JakartaHttpSessionWrapper(HttpSession session) {
        this.session = session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    /**
     * Returns the original HttpSession.
     *
     * @return original HttpSession
     */
    public HttpSession getOriginalSession() {
        return session;
    }
}
