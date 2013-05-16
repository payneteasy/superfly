package com.payneteasy.superfly.wicket;

import org.apache.wicket.core.request.handler.*;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

public class PageInterceptingRequestMapperLogic {
    public static IRequestHandler resolve(Request request, IRequestHandler handler,
            InterceptionDecisions interceptionDecisions,
            Class<? extends IRequestablePage> interceptorPageClass) {
        if (interceptionDecisions.shouldIntercept(request)) {
            boolean alreadyChanging = false;
            if (handler instanceof IPageRequestHandler) {
                IPageRequestHandler pageHandler = (IPageRequestHandler) handler;
                if (pageHandler.getPageClass() == interceptorPageClass) {
                    alreadyChanging = true;
                }
            }
            if (!alreadyChanging) {
                if (handler instanceof IComponentRequestHandler) {
                    IComponentRequestHandler iHandler = (IComponentRequestHandler) handler;
                    if (iHandler.getComponent().getPage().getClass() == interceptorPageClass) {
                        alreadyChanging = true;
                    }
                }
            }
            if (!alreadyChanging) {
                handler = new RenderPageRequestHandler(new PageProvider(interceptorPageClass));
            }
        }
        return handler;
    }

}
