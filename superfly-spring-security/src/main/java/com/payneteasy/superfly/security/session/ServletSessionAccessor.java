package com.payneteasy.superfly.security.session;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author rpuch
 */
@Service
public class ServletSessionAccessor implements ISessionAccessor {
    @Override
    public void putAttribute(String key, Object value) {
        RequestContextHolder.currentRequestAttributes().setAttribute(key, value, RequestAttributes.SCOPE_SESSION);
    }

    @Override
    public <T> T getAttribute(String key) {
        @SuppressWarnings("unchecked") T result = (T) RequestContextHolder.currentRequestAttributes().getAttribute(key,
                RequestAttributes.SCOPE_SESSION);
        return result;
    }

    @Override
    public <T> T removeAttribute(String key) {
        T result = getAttribute(key);
        RequestContextHolder.currentRequestAttributes().removeAttribute(key, RequestAttributes.SCOPE_SESSION);
        return result;
    }
}
