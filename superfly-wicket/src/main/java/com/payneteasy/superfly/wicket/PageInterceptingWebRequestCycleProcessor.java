package com.payneteasy.superfly.wicket;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.request.RequestParameters;

public class PageInterceptingWebRequestCycleProcessor extends
		WebRequestCycleProcessor {
	
	private final Class<? extends Page> pageClass;
	private final InterceptionDecisions interceptionDecisions;
	
	public PageInterceptingWebRequestCycleProcessor(
			Class<? extends Page> pageClass,
			InterceptionDecisions interceptionDecisions) {
		super();
		this.pageClass = pageClass;
		this.interceptionDecisions = interceptionDecisions;
	}

	@Override
	public IRequestTarget resolve(RequestCycle requestCycle,
			RequestParameters requestParameters) {
		IRequestTarget target = super.resolve(requestCycle, requestParameters);
		return PageInterceptingWebRequestCycleProcessorLogic.resolve(
				requestCycle, requestParameters, target,
				interceptionDecisions, pageClass);
	}
}
