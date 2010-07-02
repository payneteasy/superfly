package com.payneteasy.superfly.notification;

import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Notification about a log-out. It means that some Superfly sessions have been
 * invalidated.
 * 
 * @author Roman Puchkovskiy
 */
public class LogoutNotification extends AbstractNotification {
	private static final long serialVersionUID = 3921708726957920760L;
	
	private List<String> sessionIds;

	public List<String> getSessionIds() {
		return sessionIds;
	}

	public void setSessionIds(List<String> sessionIds) {
		this.sessionIds = sessionIds;
	}
	
	@Override
	public String toString() {
		return "LogoutNotification [for " + getCallbackUri() + "]: "
				+ StringUtils.collectionToCommaDelimitedString(sessionIds);
	}
}
