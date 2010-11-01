package com.payneteasy.superfly.wicket;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequestCycleProcessor;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;

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
		if (interceptionDecisions.shouldIntercept(requestCycle, requestParameters)) {
			boolean alreadyChanging = false;
			if (target instanceof IBookmarkablePageRequestTarget) {
				IBookmarkablePageRequestTarget bpTarget = (IBookmarkablePageRequestTarget) target;
				if (bpTarget.getPageClass() == pageClass) {
					alreadyChanging = true;
				}
			}
			if (!alreadyChanging) {
				if (target instanceof IListenerInterfaceRequestTarget) {
					IListenerInterfaceRequestTarget iTarget = (IListenerInterfaceRequestTarget) target;
					if (iTarget.getPage().getClass() == pageClass) {
						alreadyChanging = true;
					}
				}
			}
			if (!alreadyChanging) {
				target = new BookmarkablePageRequestTarget(pageClass);
			}
		}
		return target;
	}
}
