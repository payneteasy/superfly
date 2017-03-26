package com.payneteasy.superfly.notification.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.payneteasy.superfly.notification.LogoutNotification;

/**
 * Stores notifications in memory.
 * 
 * @author Roman Puchkovskiy
 */
public class InMemoryNotificationStorageStrategy implements
        NotificationStorageStrategy {

    private Queue<LogoutNotification> logoutNotifications = new ConcurrentLinkedQueue<LogoutNotification>();

    public void enqueueLogoutNotifications(
            List<LogoutNotification> notifications) {
        logoutNotifications.addAll(logoutNotifications);
    }

    public List<LogoutNotification> dequeueLogoutNotifications() {
        List<LogoutNotification> notifications = new ArrayList<LogoutNotification>();
        while (!logoutNotifications.isEmpty()) {
            notifications.add(logoutNotifications.poll());
        }
        return notifications;
    }

}
