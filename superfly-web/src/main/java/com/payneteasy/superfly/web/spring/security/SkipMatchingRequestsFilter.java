package com.payneteasy.superfly.web.spring.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * Runs the delegate only for requests that do not match {@code skipMatcher}. Unlike
 * {@code web.ignoring()} the request still goes through the rest of the chain (security headers, etc.).
 */
class SkipMatchingRequestsFilter extends GenericFilterBean {
    private final Filter         delegate;
    private final RequestMatcher skipMatcher;

    SkipMatchingRequestsFilter(Filter delegate, RequestMatcher skipMatcher) {
        this.delegate = delegate;
        this.skipMatcher = skipMatcher;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (skipMatcher.matches((HttpServletRequest) request)) {
            chain.doFilter(request, response);
        } else {
            delegate.doFilter(request, response, chain);
        }
    }
}
