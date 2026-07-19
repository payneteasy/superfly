package com.payneteasy.superfly.client.session;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Unified filter for handling logout notifications from Superfly.
 * <p>
 * This filter replaces SuperflyNotificationSinkFilter and LogoutNotificationSinkFilter.
 */
public class SuperflyLogoutFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyLogoutFilter.class);
    private final LogoutService logoutService;

    /**
     * Default constructor that creates a LogoutService instance.
     */
    public SuperflyLogoutFilter() {
        this(new LogoutService());
    }

    /**
     * Constructor with the ability to inject LogoutService (for testing).
     *
     * @param logoutService service for handling logout
     */
    public SuperflyLogoutFilter(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if ("POST".equals(request.getMethod())) {
            String logoutSessionIds = request.getParameter(LogoutService.LOGOUT_SESSION_IDS_PARAM);
            if (logoutSessionIds != null) {
                logger.debug("Intercepted logout request for sessions: {}", logoutSessionIds);
                if (logoutService.handleLogout(logoutSessionIds)) {
                    logger.debug("Logout successfully completed, breaking the filter chain");
                    return;
                }
            }
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
