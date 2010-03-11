package com.payneteasy.superfly.notification;

import java.util.List;

/**
 * Notification about a log-out. It means that some Superfly sessions have been
 * invalidated.
 * 
 * @author Roman Puchkovskiy
 */
public class LogoutNotification extends AbstractNotification {
	private List<String> sessionIds;

	public List<String> getSessionIds() {
		return sessionIds;
	}

	public void setSessionIds(List<String> sessionIds) {
		this.sessionIds = sessionIds;
	}
}
