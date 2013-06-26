package com.payneteasy.superfly.wicket;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;

/**
 * @author rpuch
 */
public class PageInterceptingRequestMapper implements IRequestMapper {
    private final IRequestMapper delegate;
    private final InterceptionDecisions interceptionDecisions;
    private final Class<? extends IRequestablePage> interceptorPageClass;

    public PageInterceptingRequestMapper(IRequestMapper delegate, InterceptionDecisions interceptionDecisions,
            Class<? extends IRequestablePage> interceptorPageClass) {
        this.delegate = delegate;
        this.interceptionDecisions = interceptionDecisions;
        this.interceptorPageClass = interceptorPageClass;
    }

    @Override
    public IRequestHandler mapRequest(Request request) {
        IRequestHandler handler = delegate.mapRequest(request);
        if (handler == null) {
            return handler;
        } else {
            return PageInterceptingRequestMapperLogic.resolve(request, handler,
                    interceptionDecisions, interceptorPageClass);
        }
    }

    @Override
    public int getCompatibilityScore(Request request) {
        return delegate.getCompatibilityScore(request);
    }

    @Override
    public Url mapHandler(IRequestHandler requestHandler) {
        return delegate.mapHandler(requestHandler);
    }
}
