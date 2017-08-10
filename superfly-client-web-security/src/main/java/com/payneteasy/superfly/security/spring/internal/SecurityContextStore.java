package com.payneteasy.superfly.security.spring.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class SecurityContextStore {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityContextStore.class);

    private static final ThreadLocal<SecurityContext> THREAD_LOCAL = new ThreadLocal<>();
    private static final String CONTEXT_ATTRIBUTE_KEY = "com.superflyapp.security.security-context";

    public static void setToThreadLocal(SecurityContext aContext) {
        THREAD_LOCAL.set(aContext);
    }

    public static void clearFromThreadLocal() {
        THREAD_LOCAL.remove();
    }

    public static SecurityContext findInThreadLocal() {
        SecurityContext context = THREAD_LOCAL.get();
        if(context == null) {
            throw new IllegalStateException("No security context in thread local. Did you install SecurityFilter?");
        }
        return context;
    }

    public static SecurityContext getSecurityContext(HttpServletRequest aRequest) {
        Object attribute = aRequest.getSession().getAttribute(CONTEXT_ATTRIBUTE_KEY);
        LOG.debug("Getting security context: {}", attribute);
        return attribute != null ? (SecurityContext) attribute : null;
    }

    public static void setToSession(SecurityContext aContext, HttpServletRequest aRequest) {
        LOG.debug("Setting security context: {}", aContext);
        aRequest.getSession().setAttribute(CONTEXT_ATTRIBUTE_KEY, aContext);
    }

    public static void clearFromSession(HttpServletRequest aRequest) {
        LOG.debug("Clear security context from request");
        aRequest.getSession().removeAttribute(CONTEXT_ATTRIBUTE_KEY);
        aRequest.getSession().invalidate();
    }
}
