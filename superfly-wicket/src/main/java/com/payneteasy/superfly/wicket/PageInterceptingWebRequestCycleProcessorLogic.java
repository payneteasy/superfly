package com.payneteasy.superfly.wicket;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;

public class PageInterceptingWebRequestCycleProcessorLogic {
	public static IRequestTarget resolve(RequestCycle requestCycle,
			RequestParameters requestParameters,
			IRequestTarget target,
			InterceptionDecisions interceptionDecisions,
			Class<? extends Page> pageClass) {
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
