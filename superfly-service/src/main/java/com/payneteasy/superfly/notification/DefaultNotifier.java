package com.payneteasy.superfly.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.payneteasy.superfly.service.job.SendNotificationOnceJob;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.service.job.TryToSendNotificationJob;
import com.payneteasy.superfly.utils.SchedulerUtils;

/**
 * Default Notifier implementation which delegates some actions to strategies.
 * 
 * @author Roman Puchkovskiy
 */
public class DefaultNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotifier.class);

    private Scheduler scheduler;
    private String sendStrategyBeanName;
    private int maxRetries = 3;

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Autowired
    public void setSendStrategyBeanName(String sendStrategyBeanName) {
        this.sendStrategyBeanName = sendStrategyBeanName;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void notifyAboutLogout(List<LogoutNotification> notifications) {
        logger.info("Notifying about logout: " + notifications);
        for (LogoutNotification notification : notifications) {
            createJob(notification);
        }
    }

    public void notifyAboutUsersChanged(List<UsersChangedNotification> notifications) {
        logger.info("Notifying about users changed: " + notifications);
        for (UsersChangedNotification notification : notifications) {
            createJob(notification);
        }
    }

    private void createJob(AbstractNotification notification) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("beanName", sendStrategyBeanName);
        dataMap.put("notification", notification);

        dataMap.put("retriesLeft", maxRetries); // deprecated

        try {
            SchedulerUtils.scheduleJob(scheduler, SendNotificationOnceJob.class, dataMap);
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
