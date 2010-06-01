package com.payneteasy.superfly.jira.filter;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.payneteasy.superfly.api.Notifications;
import com.payneteasy.superfly.client.session.AbstractSessionStoreAwareNotificationSinkFilter;
import com.payneteasy.superfly.jira.SuperflyContextLocator;

/**
 * Called by a Superfly server when users info is changed and users need to
 * be refetched.
 * 
 * @author Roman Puchkovskiy
 */
public class UsersChangedNotificationSinkFilter extends
		AbstractSessionStoreAwareNotificationSinkFilter {
	
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
	
}
