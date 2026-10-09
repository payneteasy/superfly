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
import java.util.ArrayDeque;
import java.util.Deque;

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
        path     = getApplicationPath(request);
    }

    public String getPath() {
        return path;
    }

    static String getApplicationPath(HttpServletRequest aRequest) {
        String uri = aRequest.getRequestURI();
        String contextPath = aRequest.getContextPath();
        String rawPath = contextPath != null && uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        String lower = rawPath.toLowerCase();
        if (lower.contains("%2f") || lower.contains("%5c")) {
            // an encoded separator is not a path boundary: keep the raw segment so it matches nothing
            return normalize(rawPath);
        }
        // servlet path and path info are decoded and normalized by the container
        String servletPath = aRequest.getServletPath();
        String pathInfo = aRequest.getPathInfo();
        String path = (servletPath != null ? servletPath : "") + (pathInfo != null ? pathInfo : "");
        return normalize(path.isEmpty() ? rawPath : path);
    }

    /**
     * Removes path parameters (';...') and empty segments and resolves '.' and
     * '..' segments; a '..' going above the root is kept as is.
     */
    static String normalize(String aPath) {
        Deque<String> segments = new ArrayDeque<>();
        for (String segment : aPath.split("/")) {
            int semicolon = segment.indexOf(';');
            if (semicolon >= 0) {
                segment = segment.substring(0, semicolon);
            }
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            if (segment.equals("..")) {
                if (segments.isEmpty() || segments.peekLast().equals("..")) {
                    // escapes the root: keep it so the path never matches an excluded one
                    segments.addLast(segment);
                } else {
                    segments.pollLast();
                }
                continue;
            }
            segments.addLast(segment);
        }
        if (segments.isEmpty()) {
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            sb.append('/').append(segment);
        }
        if (aPath.endsWith("/")) {
            sb.append('/');
        }
        return sb.toString();
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
