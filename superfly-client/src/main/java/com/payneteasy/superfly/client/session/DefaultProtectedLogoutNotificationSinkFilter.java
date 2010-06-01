package com.payneteasy.superfly.client.session;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.payneteasy.superfly.client.utils.CommonUtils;
import com.payneteasy.superfly.common.utils.StringUtils;

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
		String resource = getPropertiesResource(filterConfig);
		Properties properties = CommonUtils.loadPropertiesThrowing(resource);
		String commaDelimited = properties.getProperty("notification.allowed.ips").trim();
		if (commaDelimited.length() > 0) {
			String[] fragments = StringUtils.commaDelimitedListToStringArray(commaDelimited);
			
			allowedIps = new HashSet<String>();
			for (String ip : fragments) {
				allowedIps.add(ip);
			}
		}
	}

	protected String getPropertiesResource(FilterConfig filterConfig) {
		return filterConfig.getInitParameter("propertiesResource");
	}

	@Override
	protected boolean isAllowed(HttpServletRequest request) {
		return allowedIps == null || allowedIps.contains(request.getRemoteAddr());
	}
}
