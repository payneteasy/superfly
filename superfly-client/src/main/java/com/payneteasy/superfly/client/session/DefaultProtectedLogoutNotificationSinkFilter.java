package com.payneteasy.superfly.client.session;

import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * {@link LogoutNotificationSinkFilter} which is protected by a white list of
 * allowed caller IPs.
 * 
 * @author Roman Puchkovskiy
 */
public class DefaultProtectedLogoutNotificationSinkFilter extends
		LogoutNotificationSinkFilter {
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
