package com.payneteasy.superfly.utils;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.Map;

public class SchedulerUtils {

    private static final String GROUP = "sender";

    public static void scheduleJob(Scheduler scheduler,
            Class<? extends Job> jobClass, Map<String, Object> dataMap)
            throws SchedulerException {
        String name = new RandomGUID().toString();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(name, GROUP)
                .usingJobData(new JobDataMap(dataMap))
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, GROUP)
                .startAt(new Date(new Date().getTime() + 1000))
                .build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public static void rescheduleJob(JobExecutionContext context, Map<String, Object> dataMap) throws SchedulerException {
        String name = new RandomGUID().toString();
        JobDetail jobDetail = JobBuilder.newJob(context.getJobDetail().getJobClass())
                .withIdentity(name, GROUP)
                .usingJobData(new JobDataMap(dataMap))
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, GROUP)
                .startAt(new Date(new Date().getTime() + 1000 * 60))
                .build();
        context.getScheduler().scheduleJob(jobDetail, trigger);
    }
}
