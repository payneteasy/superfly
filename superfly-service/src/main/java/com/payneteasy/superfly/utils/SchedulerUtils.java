package com.payneteasy.superfly.utils;

import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class SchedulerUtils {
	
	private static final String GROUP = "sender";
	
	public static void scheduleJob(Scheduler scheduler,
			Class<? extends Job> jobClass, Map<String, Object> dataMap)
			throws SchedulerException {
		String name = new RandomGUID().toString();
		JobDetail jobDetail = new JobDetail(name, GROUP, jobClass);
		jobDetail.getJobDataMap().putAll(dataMap);
		Trigger trigger = new SimpleTrigger(name, GROUP, new Date(new Date().getTime() + 1000));
		scheduler.scheduleJob(jobDetail, trigger);
	}
	
	public static void rescheduleJob(JobExecutionContext context, Map<String, Object> dataMap) throws SchedulerException {
		String name = new RandomGUID().toString();
		JobDetail jobDetail = new JobDetail(name, GROUP, context.getJobDetail().getJobClass());
		jobDetail.getJobDataMap().putAll(dataMap);
		Trigger trigger = new SimpleTrigger(name, GROUP, new Date(new Date().getTime() + 1000 * 60));
		context.getScheduler().scheduleJob(jobDetail, trigger);
	}
}
