package com.payneteasy.superfly.wicket;

import org.apache.wicket.request.Request;

/**
 * Strategy interface used by PageInterceptingRequestMapper for EE8 Wicket applications.
 */
public interface InterceptionDecisions {
    boolean shouldIntercept(Request requestCycle);
}

