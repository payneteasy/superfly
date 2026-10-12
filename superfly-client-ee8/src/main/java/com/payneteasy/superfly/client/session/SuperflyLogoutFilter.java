package com.payneteasy.superfly.client.session;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Unified filter for handling logout notifications from Superfly (Java EE 8 / javax).
 * <p>
 * Only notifications signed by the server are processed, others are answered with 403. The subsystem token
 * used to verify signatures is taken from the {@value NotificationSignatureVerifier#TOKEN_INIT_PARAMETER}
 * init-param or {@link #setNotificationSecret(String)}.
 */
public class SuperflyLogoutFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(SuperflyLogoutFilter.class);
    private final LogoutService logoutService;
    private final NotificationSignatureVerifier signatureVerifier = new NotificationSignatureVerifier();

    public SuperflyLogoutFilter() {
        this(new LogoutService());
    }

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
                if (!signatureVerifier.isAuthentic(request.getParameterMap(), request.getRemoteAddr())) {
                    ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
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
        signatureVerifier.configure(filterConfig.getInitParameter(NotificationSignatureVerifier.TOKEN_INIT_PARAMETER), null);
    }

    /**
     * @param subsystemToken the subsystem token (as issued by Superfly) to verify notification signatures with
     */
    public void setNotificationSecret(String subsystemToken) {
        signatureVerifier.setSubsystemToken(subsystemToken);
    }

    @Override
    public void destroy() {
    }
}
