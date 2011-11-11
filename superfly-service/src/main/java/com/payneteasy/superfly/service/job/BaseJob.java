package com.payneteasy.superfly.service.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
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

        final SchedulerContext schedulerContext = context.getScheduler().getContext();
        ApplicationContext applicationContext =  (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);

        // show keys in context
        if(applicationContext==null) {
            logger.error(APPLICATION_CONTEXT_KEY+" is empty in "+ schedulerContext+":");
            if(schedulerContext.getKeys()!=null) {
                for (String key : schedulerContext.getKeys()) {
                    Object value = schedulerContext.get(key);
                    String valueText = value!=null ? value.toString() : "<NULL>";
                    logger.info("    {} = {}", key, valueText);
                }
            }
        }

        return applicationContext;
	}
	
	protected NotificationSendStrategy getNotificationSendStrategy(JobExecutionContext context, String beanName)
			throws BeansException, SchedulerException {

        ApplicationContext applicationContext = getApplicationContext(context);
        return applicationContext!=null ? (NotificationSendStrategy) applicationContext.getBean(beanName) : null;

	}
	
	/**
	 * @see org.springframework.scheduling.quartz.QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
	 */
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if(context==null) {
            // todo this quick fix for:
            // ERROR ErrorLogger                - Job (sender.9F9F6217-536B-630D-4B8F-A2A4D5F74011 threw an exception.
            //org.quartz.SchedulerException: Job threw an unhandled exception. [See nested exception: java.lang.NullPointerException]
            //        at org.quartz.core.JobRunShell.run(JobRunShell.java:213)
            //        at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:525)
            //Caused by: java.lang.NullPointerException
            //        at com.payneteasy.superfly.service.job.BaseJob.getNotificationSendStrategy(BaseJob.java:40)
            //        at com.payneteasy.superfly.service.job.SendNotificationOnceJob.doExecute(SendNotificationOnceJob.java:39)
            //        at com.payneteasy.superfly.service.job.BaseJob.executeInternal(BaseJob.java:50)
            //        at org.springframework.scheduling.quartz.QuartzJobBean.execute(QuartzJobBean.java:86)
            //        at org.quartz.core.JobRunShell.run(JobRunShell.java:202)
            logger.error("Something went wrong, no JobExecutionContext was passed for "+getClass().getSimpleName()+" job");
        } else {
            try {
                doExecute(context);
            } catch (SchedulerException e) {
                logger.error("Scheduler error", e);
                throw new JobExecutionException(e);
            }
        }
	}
	
	protected abstract void doExecute(JobExecutionContext context) throws SchedulerException;
}
