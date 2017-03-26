package com.payneteasy.superfly.wicket;

import org.apache.wicket.request.Request;

public interface InterceptionDecisions {
    boolean shouldIntercept(Request requestCycle);
}
