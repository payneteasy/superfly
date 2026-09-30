package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.session.SessionMappingLocator;
import com.payneteasy.superfly.common.utils.StringUtils;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter that accepts notifications from the Superfly server (Java EE 8).
 *
 * @deprecated Use {@link SuperflyLogoutFilter}
 */
@Deprecated
public class SuperflyNotificationSinkFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyNotificationSinkFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equals(request.getMethod())) {
            String logoutSessionIds = request.getParameter(getLogoutSessionIdsParameterName());
            if (logoutSessionIds != null) {
                String[] sessionIds = StringUtils.commaDelimitedListToStringArray(logoutSessionIds);
                for (String key : sessionIds) {
                    HttpSessionWrapper session = getSessionMapping().removeSessionByKey(key);
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

    protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
        return SessionMappingLocator.getSessionMapping();
    }

    protected String getLogoutSessionIdsParameterName() {
        return "superflyLogoutSessionIds";
    }

    @Override
    public void destroy() {
    }
}
