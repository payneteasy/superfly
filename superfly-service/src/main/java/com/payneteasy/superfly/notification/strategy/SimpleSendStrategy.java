package com.payneteasy.superfly.notification.strategy;

import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.util.StringUtils;

import com.payneteasy.superfly.api.Notifications;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;

/**
 * Simple HTTP send strategy which just makes a single request.
 * 
 * @author Roman Puchkovskiy
 */
public class SimpleSendStrategy extends AbstractHttpNotificationSendStrategy {
	
	public void send(final LogoutNotification notification)
			throws NotificationException {
		Runnable task = new Runnable() {
			public void run() {
				doCall(notification.getCallbackUri(), Notifications.LOGOUT, new ParameterSetter() {
					public void setParameters(PostMethod httpMethod) {
						httpMethod.setParameter("superflyLogoutSessionIds",
								StringUtils.collectionToCommaDelimitedString(notification.getSessionIds()));
					}
				});
			}
		};
		doExecute(task);
	}

	public void send(final UsersChangedNotification notification)
			throws NotificationException {
		Runnable task = new Runnable() {
			public void run() {
				doCall(notification.getCallbackUri(), Notifications.USERS_CHANGED, new ParameterSetter() {
					public void setParameters(PostMethod httpMethod) {
					}
				});
			}
		};
		doExecute(task);
	}
}
