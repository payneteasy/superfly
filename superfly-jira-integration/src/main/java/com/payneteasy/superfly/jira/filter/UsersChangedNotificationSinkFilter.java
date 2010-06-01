package com.payneteasy.superfly.jira.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.payneteasy.superfly.api.Notifications;
import com.payneteasy.superfly.client.session.AbstractSessionStoreAwareNotificationSinkFilter;
import com.payneteasy.superfly.client.utils.CommonUtils;
import com.payneteasy.superfly.common.utils.StringUtils;
import com.payneteasy.superfly.jira.SuperflyContextLocator;

/**
 * Called by a Superfly server when users info is changed and users need to
 * be refetched.
 * 
 * @author Roman Puchkovskiy
 */
public class UsersChangedNotificationSinkFilter extends
		AbstractSessionStoreAwareNotificationSinkFilter {
	
	private Set<String> allowedIps = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		String resource = getPropertiesResource(filterConfig);
		Properties properties = CommonUtils.loadPropertiesThrowing(resource);
		String commaDelimited = properties.getProperty("notification.allowed.ips").trim();
		String[] fragments = StringUtils.commaDelimitedListToStringArray(commaDelimited);
		
		allowedIps = new HashSet<String>();
		for (String ip : fragments) {
			allowedIps.add(ip);
		}
	}
	
	protected String getPropertiesResource(FilterConfig filterConfig) {
		return filterConfig.getInitParameter("propertiesResource");
	}
	
	@Override
	protected boolean acceptsNotificationType(String notificationType) {
		return Notifications.USERS_CHANGED.equals(notificationType);
	}

	@Override
	protected boolean doFilterRequest(HttpServletRequest request) {
		Collection<HttpSession> sessions = getSessionMapping().clear();
		for (HttpSession session : sessions) {
			invalidateSessionQuietly(session);
		}
		SuperflyContextLocator.getSuperflyContext().getUserStoreUpdater().runUpdate();
		return true;
	}
	
	@Override
	protected boolean isAllowed(HttpServletRequest request) {
		return allowedIps.contains(request.getRemoteAddr());
	}
	
}
