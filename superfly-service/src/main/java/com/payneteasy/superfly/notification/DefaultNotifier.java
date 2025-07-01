package com.payneteasy.superfly.notification;

import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import com.payneteasy.superfly.service.job.SendNotificationOnceJob;
import com.payneteasy.superfly.utils.SchedulerUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default Notifier implementation which delegates some actions to strategies.
 *
 * @author Roman Puchkovskiy
 */
@Slf4j
@Component
public class DefaultNotifier implements Notifier {

    private Scheduler scheduler;
    private String    sendStrategyBeanName;
    @Setter
    private int       maxRetries = 3;

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Autowired
    public void setSendStrategyBeanName(ApplicationContext context, NotificationSendStrategy sendStrategyBeanName) {
        Objects.requireNonNull(context, "context");
        String[] beanNamesForType = context.getBeanNamesForType(sendStrategyBeanName.getClass());
        Objects.checkIndex(0, beanNamesForType.length);

        this.sendStrategyBeanName = beanNamesForType[0];
    }

    public void notifyAboutLogout(List<LogoutNotification> notifications) {
        log.info("Notifying about logout: {}", notifications);
        for (LogoutNotification notification : notifications) {
            createJob(notification);
        }
    }

    public void notifyAboutUsersChanged(List<UsersChangedNotification> notifications) {
        log.info("Notifying about users changed: {}", notifications);
        for (UsersChangedNotification notification : notifications) {
            createJob(notification);
        }
    }

    private void createJob(AbstractNotification notification) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("beanName", sendStrategyBeanName);
        dataMap.put("notification", notification);

        dataMap.put("retriesLeft", maxRetries); // deprecated

        try {
            SchedulerUtils.scheduleJob(scheduler, SendNotificationOnceJob.class, dataMap);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
}
