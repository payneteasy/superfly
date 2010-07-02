package com.payneteasy.superfly.service.job;

import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.notification.AbstractNotification;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import com.payneteasy.superfly.utils.SchedulerUtils;

public class TryToSendNotificationJob extends BaseJob {
	
	private static final Logger logger = LoggerFactory.getLogger(TryToSendNotificationJob.class);
	
	private String beanName;
	private AbstractNotification notification;
	private int retriesLeft;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setNotification(AbstractNotification notification) {
		this.notification = notification;
	}

	public void setRetriesLeft(int retriesLeft) {
		this.retriesLeft = retriesLeft;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doExecute(JobExecutionContext context)
			throws SchedulerException {
		NotificationSendStrategy sendStrategy = getNotificationSendStrategy(context, beanName);
		try {
			if (notification instanceof LogoutNotification) {
				sendStrategy.send((LogoutNotification) notification);
			} else if (notification instanceof UsersChangedNotification) {
				sendStrategy.send((UsersChangedNotification) notification);
			} else {
				throw new IllegalArgumentException("Unknown notification type: " + notification.getClass());
			}
		} catch (NotificationException e) {
			if (retriesLeft > 0) {
				logger.warn("Error while trying to send a notification, retrying "
						+ retriesLeft + " more times: " + notification.toString(), e);
				Map<String, Object> dataMap = context.getJobDetail().getJobDataMap();
				dataMap.put("retriesLeft", retriesLeft - 1);
				SchedulerUtils.rescheduleJob(context, dataMap);
			} else {
				logger.error("Error while trying to send a notification, no more retries, dropping: " + notification.toString(), e);
			}
		}
	}

}
