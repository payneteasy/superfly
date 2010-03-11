package com.payneteasy.superfly.notification;

/**
 * Base for all subsystem notifications.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractNotification {
	private String callbackUri;

	public String getCallbackUri() {
		return callbackUri;
	}

	public void setCallbackUri(String callbackUri) {
		this.callbackUri = callbackUri;
	}
}
