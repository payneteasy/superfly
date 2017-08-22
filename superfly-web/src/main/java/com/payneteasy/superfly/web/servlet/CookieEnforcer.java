package com.payneteasy.superfly.web.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * @author rpuch
 */
public class CookieEnforcer implements Filter {
    private boolean enforceSecure = true;
    private boolean enforceHttpOnly = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(response) {
            @Override
            public void addCookie(Cookie cookie) {
                Cookie clone = (Cookie) cookie.clone();
                if (enforceSecure) {
                    clone.setSecure(true);
                }
                if (enforceHttpOnly) {
                    clone.setHttpOnly(true);
                }
                super.addCookie(clone);
            }
        };
        chain.doFilter(req, wrappedResponse);
    }

    @Override
    public void destroy() {

    }
}
