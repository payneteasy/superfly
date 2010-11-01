package com.payneteasy.superfly.wicket;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.RequestParameters;

public interface InterceptionDecisions {
	boolean shouldIntercept(RequestCycle requestCycle,
			RequestParameters requestParameters);
}
