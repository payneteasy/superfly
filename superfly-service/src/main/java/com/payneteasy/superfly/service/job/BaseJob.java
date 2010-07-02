package com.payneteasy.superfly.service.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;

/**
 * Base job class which gives access to the ApplicationContext and some its
 * beans.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class BaseJob extends QuartzJobBean {
	private static Logger logger = LoggerFactory.getLogger(BaseJob.class);

	private static final String APPLICATION_CONTEXT_KEY = "applicationContext";

	/**
	 * Obtains Spring's application context.
	 * 
	 * @param context	job execution context
	 * @return application context
	 * @throws SchedulerException
	 */
	protected ApplicationContext getApplicationContext(JobExecutionContext context)
			throws SchedulerException {
		return (ApplicationContext) context.getScheduler().getContext()
				.get(APPLICATION_CONTEXT_KEY);
	}
	
	protected NotificationSendStrategy getNotificationSendStrategy(JobExecutionContext context, String beanName)
			throws BeansException, SchedulerException {
		return (NotificationSendStrategy) getApplicationContext(context).getBean(beanName);
	}
	
	/**
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		try {
			doExecute(context);
		} catch (SchedulerException e) {
			logger.error("Scheduler error", e);
			throw new JobExecutionException(e);
		}
	}
	
	protected abstract void doExecute(JobExecutionContext context) throws SchedulerException;
}
