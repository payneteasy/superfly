package com.payneteasy.superfly.wicket;

import org.apache.wicket.core.request.handler.BufferedResponseRequestHandler;
import org.apache.wicket.core.request.handler.IComponentRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestHandlerDelegate;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

/**
 * Shared interception logic for both EE8 and EE10 Wicket integrations.
 */
public class PageInterceptingRequestMapperLogic {
    public static IRequestHandler resolve(Request request, IRequestHandler handler,
                                          InterceptionDecisions interceptionDecisions,
                                          Class<? extends IRequestablePage> interceptorPageClass) {
        IRequestHandler result = handler;
        if (interceptionDecisions.shouldIntercept(request)) {
            boolean alreadyChanging = false;
            IRequestHandler localHandler = unwrapDelegates(handler);
            if (localHandler instanceof IPageRequestHandler) {
                IPageRequestHandler pageHandler = (IPageRequestHandler) localHandler;
                if (pageHandler.getPageClass() == interceptorPageClass) {
                    alreadyChanging = true;
                }
            }
            if (!alreadyChanging) {
                if (localHandler instanceof IComponentRequestHandler) {
                    IComponentRequestHandler iHandler = (IComponentRequestHandler) localHandler;
                    if (iHandler.getComponent().getPage().getClass() == interceptorPageClass) {
                        alreadyChanging = true;
                    }
                }
            }
            boolean bufferedResponse = (localHandler instanceof BufferedResponseRequestHandler);
            if (!alreadyChanging && !bufferedResponse) {
                result = new RenderPageRequestHandler(new PageProvider(interceptorPageClass));
            }
        }
        return result;
    }

    private static IRequestHandler unwrapDelegates(IRequestHandler handler) {
        while (handler instanceof IRequestHandlerDelegate) {
            handler = ((IRequestHandlerDelegate) handler).getDelegateHandler();
        }
        return handler;
    }

}

