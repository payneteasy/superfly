package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.client.utils.CommonUtils;
import com.payneteasy.superfly.common.utils.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Base for filters that accept notifications from a Superfly server (Java EE 8).
 * <p>
 * Only notifications signed by the server are processed, others are answered with 403. The subsystem token
 * used to verify signatures is taken from the {@value NotificationSignatureVerifier#TOKEN_INIT_PARAMETER}
 * init-param, or the {@value NotificationSignatureVerifier#TOKEN_PROPERTY} property of the 'propertiesResource',
 * or {@link #setNotificationSecret(String)}.
 */
public abstract class AbstractNotificationSinkFilter implements Filter {

    private final NotificationSignatureVerifier signatureVerifier = new NotificationSignatureVerifier();

    protected abstract boolean doFilterRequest(HttpServletRequest request);

    protected abstract boolean acceptsNotificationType(String notificationType);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        signatureVerifier.configure(filterConfig.getInitParameter(NotificationSignatureVerifier.TOKEN_INIT_PARAMETER),
                getPropertiesResource(filterConfig));
    }

    /**
     * @param subsystemToken the subsystem token (as issued by Superfly) to verify notification signatures with
     */
    public void setNotificationSecret(String subsystemToken) {
        signatureVerifier.setSubsystemToken(subsystemToken);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equals(request.getMethod())) {
            String notificationType = request.getParameter(getSuperflyNotificationParameterName());
            if (notificationType != null && acceptsNotificationType(notificationType)) {
                if (isAllowed(request)) {
                    if (!signatureVerifier.isAuthentic(request.getParameterMap(), request.getRemoteAddr())) {
                        ((HttpServletResponse) resp).sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    if (doFilterRequest(request)) {
                        return;
                    }
                }
            }
        }
        chain.doFilter(req, resp);
    }

    protected boolean isAllowed(HttpServletRequest request) {
        return true;
    }

    protected String getSuperflyNotificationParameterName() {
        return "superflyNotification";
    }

    @Override
    public void destroy() {
    }

    protected Set<String> initAllowedIps(FilterConfig filterConfig) {
        String resource = getPropertiesResource(filterConfig);
        Set<String> ips = null;
        Properties properties = CommonUtils.loadPropertiesThrowing(resource);
        String commaDelimited = properties.getProperty("notification.allowed.ips").trim();
        if (commaDelimited.length() > 0) {
            String[] fragments = StringUtils.commaDelimitedListToStringArray(commaDelimited);
            ips = new HashSet<>();
            for (String ip : fragments) {
                ips.add(ip);
            }
        }
        return ips;
    }

    protected String getPropertiesResource(FilterConfig filterConfig) {
        return filterConfig.getInitParameter("propertiesResource");
    }

    protected boolean isAllowedByIp(HttpServletRequest request, Set<String> allowedIps) {
        return allowedIps == null || allowedIps.contains(request.getRemoteAddr());
    }
}
