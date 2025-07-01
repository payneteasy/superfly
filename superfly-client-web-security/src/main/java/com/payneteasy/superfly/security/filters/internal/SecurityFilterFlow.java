package com.payneteasy.superfly.security.filters.internal;

import com.payneteasy.superfly.security.filters.ExcludedPaths;
import com.payneteasy.superfly.security.spring.internal.SecurityContext;
import com.payneteasy.superfly.security.spring.internal.SecurityContextStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.payneteasy.superfly.security.spring.internal.SecurityContextStore.clearFromThreadLocal;
import static com.payneteasy.superfly.security.spring.internal.SecurityContextStore.getSecurityContext;
import static com.payneteasy.superfly.security.spring.internal.SecurityContextStore.setToThreadLocal;

public class SecurityFilterFlow {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityFilterFlow.class);

    final HttpServletRequest  request;
    final HttpServletResponse response;
    final String path;

    public SecurityFilterFlow(HttpServletRequest aRequest, HttpServletResponse aResponse) {
        request  = aRequest;
        response = aResponse;
        path     = request.getRequestURI().substring(request.getContextPath().length());
    }

    public String getPath() {
        return path;
    }

    public boolean processLogoutUrl() throws IOException {
        if(path.startsWith("/j_spring_security_logout")) {
            SecurityContextStore.clearFromSession(request);
            response.sendRedirect(request.getContextPath());
            return true;
        }
        return false;
    }

    public boolean processWithSecurityContext(FilterChain aChain) throws IOException, ServletException {
        SecurityContext context = getSecurityContext(request);
        if(context == null) {
            return false;
        }

        setToThreadLocal(context);
        try {
            aChain.doFilter(request, response);
            return true;
        } finally {
            clearFromThreadLocal();
        }
    }

    public boolean processExcluded(ExcludedPaths aPaths, FilterChain aChain) throws IOException, ServletException {
        // assumes these resources must be in the nginx
        if(aPaths.isExcluded(path)) {
            aChain.doFilter(request, response);
            return true;
        }
        return false;
    }
}
