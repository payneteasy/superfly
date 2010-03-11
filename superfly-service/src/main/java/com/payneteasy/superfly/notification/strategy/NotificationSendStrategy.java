package com.payneteasy.superfly.notification.strategy;

import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;

/**
 * Strategy used to send notifications.
 * 
 * @author Roman Puchkovskiy
 */
public interface NotificationSendStrategy {
	/**
	 * Sends a logout notification.
	 * 
	 * @param notification	notificatino to send
	 * @throws NotificationException
	 */
	void send(LogoutNotification notification) throws NotificationException;
	
	/**
	 * Sends a 'users changed' notification.
	 * 
	 * @param notification	notificatino to send
	 * @throws NotificationException
	 */
	void send(UsersChangedNotification notification) throws NotificationException;
}
