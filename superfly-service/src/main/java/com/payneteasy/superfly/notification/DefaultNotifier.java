package com.payneteasy.superfly.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;

/**
 * Default Notifier implementation which delegates some actions to strategies.
 * 
 * @author Roman Puchkovskiy
 */
public class DefaultNotifier implements Notifier {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultNotifier.class);
	
	private NotificationSendStrategy sendStrategy;

	@Required
	public void setSendStrategy(NotificationSendStrategy sendStrategy) {
		this.sendStrategy = sendStrategy;
	}

	public void notifyAboutLogout(List<LogoutNotification> notifications) {
		for (LogoutNotification notification : notifications) {
			try {
				sendStrategy.send(notification);
			} catch (NotificationException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void notifyAboutUsersChanged(
			List<UsersChangedNotification> notifications) {
		for (UsersChangedNotification notification : notifications) {
			try {
				sendStrategy.send(notification);
			} catch (NotificationException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

}
