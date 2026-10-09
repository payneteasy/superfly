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
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter that accepts notifications from the Superfly server (Java EE 8).
 * <p>
 * Only notifications signed by the server are processed, others are answered with 403. The subsystem token
 * used to verify signatures is taken from the {@value NotificationSignatureVerifier#TOKEN_INIT_PARAMETER}
 * init-param or {@link #setNotificationSecret(String)}.
 *
 * @deprecated Use {@link SuperflyLogoutFilter}
 */
@Deprecated
public class SuperflyNotificationSinkFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyNotificationSinkFilter.class);

    private final NotificationSignatureVerifier signatureVerifier = new NotificationSignatureVerifier();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        signatureVerifier.configure(filterConfig.getInitParameter(NotificationSignatureVerifier.TOKEN_INIT_PARAMETER), null);
    }

    /**
     * @param subsystemToken the subsystem token (as issued by Superfly) to verify notification signatures with
     */
    public void setNotificationSecret(String subsystemToken) {
        signatureVerifier.setSubsystemToken(subsystemToken);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equals(request.getMethod())) {
            String logoutSessionIds = request.getParameter(getLogoutSessionIdsParameterName());
            if (logoutSessionIds != null) {
                if (!signatureVerifier.isAuthentic(request.getParameterMap(), request.getRemoteAddr())) {
                    ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
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
