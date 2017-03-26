package com.payneteasy.superfly.client.session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.payneteasy.superfly.client.utils.CommonUtils;
import com.payneteasy.superfly.common.utils.StringUtils;

/**
 * Base for filters that accept notifications from a Superfly server.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractNotificationSinkFilter implements Filter {

    protected abstract boolean doFilterRequest(HttpServletRequest request);

    protected abstract boolean acceptsNotificationType(String notificationType);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equals(request.getMethod())) {
            String notificationType = request.getParameter(getSuperflyNotificationParameterName());
            if (notificationType != null && acceptsNotificationType(notificationType)) {
                if (isAllowed(request)) {
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

    public void destroy() {
    }

    protected Set<String> initAllowedIps(FilterConfig filterConfig) {
        String resource = getPropertiesResource(filterConfig);
        Set<String> ips = null;
        Properties properties = CommonUtils.loadPropertiesThrowing(resource);
        String commaDelimited = properties.getProperty("notification.allowed.ips").trim();
        if (commaDelimited.length() > 0) {
            String[] fragments = StringUtils.commaDelimitedListToStringArray(commaDelimited);

            ips = new HashSet<String>();
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