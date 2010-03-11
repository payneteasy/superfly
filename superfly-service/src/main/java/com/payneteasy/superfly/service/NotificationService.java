package com.payneteasy.superfly.service;

/**
 * Service used to send notifications.
 * 
 * @author Roman Puchkovskiy
 */
public interface NotificationService {
	/**
	 * Notifies all systems interested in this about the fact that users have
	 * changed.
	 */
	void notifyAboutUsersChanged();
}
