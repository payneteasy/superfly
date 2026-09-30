package com.payneteasy.superfly.notification;

import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Default Notifier implementation which delegates some actions to strategies.
 *
 * <p>Notifications are dispatched asynchronously with a short delay via Spring's
 * {@link TaskScheduler} (previously a one-shot Quartz job). The send is a single
 * attempt: on failure the notification is logged and dropped, matching the prior
 * {@code SendNotificationOnceJob} semantics.
 *
 * @author Roman Puchkovskiy
 */
@Slf4j
@Component
public class DefaultNotifier implements Notifier {

    /** Delay before dispatching, identical to the former Quartz trigger start offset. */
    private static final Duration SEND_DELAY = Duration.ofSeconds(1);

    private final TaskScheduler            taskScheduler;
    private final NotificationSendStrategy sendStrategy;

    @Autowired
    public DefaultNotifier(TaskScheduler taskScheduler, NotificationSendStrategy sendStrategy) {
        this.taskScheduler = taskScheduler;
        this.sendStrategy = sendStrategy;
    }

    public void notifyAboutLogout(List<LogoutNotification> notifications) {
        log.info("Notifying about logout: {}", notifications);
        notifications.forEach(this::scheduleSend);
    }

    public void notifyAboutUsersChanged(List<UsersChangedNotification> notifications) {
        log.info("Notifying about users changed: {}", notifications);
        notifications.forEach(this::scheduleSend);
    }

    private void scheduleSend(AbstractNotification notification) {
        log.debug("Scheduling notification send in {}: {}", SEND_DELAY, notification);
        taskScheduler.schedule(() -> send(notification), Instant.now().plus(SEND_DELAY));
    }

    private void send(AbstractNotification notification) {
        log.debug("Sending notification: {}", notification);
        try {
            if (notification instanceof LogoutNotification logoutNotification) {
                sendStrategy.send(logoutNotification);
            } else if (notification instanceof UsersChangedNotification usersChangedNotification) {
                sendStrategy.send(usersChangedNotification);
            } else {
                throw new IllegalArgumentException("Unknown notification type: " + notification.getClass());
            }
        } catch (Exception e) {
            log.error("Error while trying to send a notification, no more retries, dropping: {}",
                      notification, e);
        }
    }
}
