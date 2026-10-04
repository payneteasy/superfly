package com.payneteasy.superfly.web.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
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
                // Not conditional on req.isSecure(): TLS is terminated by a proxy, so Jetty sees plain http.
                // Browsers accept Secure cookies on http://localhost, so local runs keep working.
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
