package com.payneteasy.superfly.notification.strategy;

import java.util.List;

import com.payneteasy.superfly.notification.LogoutNotification;

/**
 * Strategy used to store before sending notifications.
 * 
 * @author Roman Puchkovskiy
 */
public interface NotificationStorageStrategy {
    /**
     * Adds logout notifications to storage.
     *
     * @param notifications    notifications to add
     */
    void enqueueLogoutNotifications(List<LogoutNotification> notifications);
    /**
     * Dequeues logout notifications.
     *
     * @return notifications
     */
    List<LogoutNotification> dequeueLogoutNotifications();
}
