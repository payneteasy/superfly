package com.payneteasy.superfly.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;

/**
 * Filter which creates a correlation between a Superfly session key and an
 * HttpSession.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOUserSessionBindFilter extends GenericFilterBean {

    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication instanceof SSOUserAuthenticationToken) {
                SSOUserAuthenticationToken token = (SSOUserAuthenticationToken) authentication;
                getSessionMapping().addSession(token.getUser().getSessionId(), session);
            }
        }
        chain.doFilter(request, response);
    }

    protected SessionMapping getSessionMapping() {
        return SessionMappingLocator.getSessionMapping();
    }

}
