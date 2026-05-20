package com.payneteasy.superfly.client.session;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * {@link LogoutNotificationSinkFilter} which is protected by a white list of
 * allowed caller IPs.
 *
 * @author Roman Puchkovskiy
 * @deprecated Use {@link SuperflyLogoutFilter}
 */
@Deprecated
public class DefaultProtectedLogoutNotificationSinkFilter extends LogoutNotificationSinkFilter {
    private Set<String> allowedIps = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        allowedIps = initAllowedIps(filterConfig);
    }

    @Override
    protected boolean isAllowed(HttpServletRequest request) {
        return isAllowedByIp(request, allowedIps);
    }
}
