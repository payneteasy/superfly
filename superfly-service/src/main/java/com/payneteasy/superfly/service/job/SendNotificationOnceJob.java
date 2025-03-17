package com.payneteasy.superfly.service.job;

import com.payneteasy.superfly.notification.AbstractNotification;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

@Setter
@Slf4j
public class SendNotificationOnceJob extends BaseJob {

    private String               beanName;
    private AbstractNotification notification;
    private int                  retriesLeft;

    @SuppressWarnings("unchecked")
    @Override
    protected void doExecute(JobExecutionContext context) throws SchedulerException {
        NotificationSendStrategy sendStrategy = getNotificationSendStrategy(context, beanName);

        if (sendStrategy == null) {
            log.info("Notification strategy is null");
            return;
        }

        try {
            if (notification instanceof LogoutNotification) {
                sendStrategy.send((LogoutNotification) notification);
            } else if (notification instanceof UsersChangedNotification) {
                sendStrategy.send((UsersChangedNotification) notification);
            } else {
                throw new IllegalArgumentException("Unknown notification type: " + notification.getClass());
            }
        } catch (NotificationException e) {
            log.error("Error while trying to send a notification, no more retries, dropping: {}",
                      notification.toString(),
                      e
            );
        }
    }

}
