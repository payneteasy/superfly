package com.payneteasy.superfly.client.session;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;
import com.payneteasy.superfly.common.utils.StringUtils;

/**
 * Filter that accepts notifications from the Superfly server.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyNotificationSinkFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(SuperflyNotificationSinkFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equals(request.getMethod())) {
            String logoutSessionIds = request.getParameter(getLogoutSessionIdsParameterName());
            if (logoutSessionIds != null) {
                // this is actually a logout request, so process it and
                // interrupt filter chain
                String[] sessionIds = StringUtils.commaDelimitedListToStringArray(logoutSessionIds);
                for (String key : sessionIds) {
                    HttpSession session = getSessionMapping().removeSessionByKey(key);
                    if (session != null) {
                        try {
                            session.invalidate();
                        } catch (IllegalStateException e) {
                            logger.warn("Ignored exception while trying to invalidate a session", e);
                        }
                    }
                }
                return;
            }
        }
        chain.doFilter(req, resp);
    }

    protected SessionMapping getSessionMapping() {
        return SessionMappingLocator.getSessionMapping();
    }

    protected String getLogoutSessionIdsParameterName() {
        return "superflyLogoutSessionIds";
    }

    public void destroy() {
    }

}
