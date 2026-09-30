package com.payneteasy.superfly.client.session;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * Protected logout notification sink filter (Java EE 8).
 *
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
