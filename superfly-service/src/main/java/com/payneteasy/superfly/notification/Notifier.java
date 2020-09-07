package com.payneteasy.superfly.notification;

import java.util.List;

/**
 * Used to notify subsystems about some events (like 'sessions have been
 * logged out').
 * 
 * @author Roman Puchkovskiy
 */
public interface Notifier {
    /**
     * Notifies subsystems about a logout of some sessions.
     *
     * @param notifications    notifications list
     */
    void notifyAboutLogout(List<LogoutNotification> notifications);

    /**
     * Notifies subsystems that users have changed.
     *
     * @param notifications    notifications list
     */
    void notifyAboutUsersChanged(List<UsersChangedNotification> notifications);
}
