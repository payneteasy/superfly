package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import com.payneteasy.superfly.notification.strategy.SimpleSendStrategy;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.UserService;
import org.apache.commons.httpclient.HttpClient;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
//@EnableScheduling // Если планируется использовать и @Scheduled
public class QuartzConfig {

    // region Job Details
    @Bean
    public MethodInvokingJobDetailFactoryBean expireSessionsJobDetail(SessionService sessionService) {
        return createJobDetail(sessionService, "deleteExpiredAndOldSessionsAndNotify", 86400);
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean expirePasswordJobDetail(UserService userService) {
        return createJobDetail(userService, "expirePasswords", 90);
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean suspendUsersJobDetail(UserService userService) {
        return createJobDetail(userService, "suspendUsers", 90);
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean expireSSOSessionsJobDetail(
            SessionService sessionService,
            @Value("#{contextParameters['superfly-max-sso-session-age-minutes']}") int ssoSessionAge) {

        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setTargetObject(sessionService);
        jobDetail.setTargetMethod("deleteExpiredSSOSessions");
        jobDetail.setArguments(new Object[]{ssoSessionAge});
        return jobDetail;
    }

    @Bean
    public MethodInvokingJobDetailFactoryBean expireTokensJobDetail(SessionService sessionService) {
        return createJobDetail(sessionService, "deleteExpiredTokens", 5);
    }
    // endregion

    // region Triggers
    @Bean
    public SimpleTriggerFactoryBean expireSessionsTrigger(
            @Qualifier("expireSessionsJobDetail") JobDetail jobDetail) {

        return createTrigger(jobDetail, 60_000); // 1 minute
    }

    @Bean
    public SimpleTriggerFactoryBean expirePasswordsTrigger(
            @Qualifier("expirePasswordJobDetail") JobDetail jobDetail) {

        return createTrigger(jobDetail, 86_400_000); // 24 hours
    }

    @Bean
    public SimpleTriggerFactoryBean suspendUsersTrigger(
            @Qualifier("suspendUsersJobDetail") JobDetail jobDetail) {

        return createTrigger(jobDetail, 86_400_000); // 24 hours
    }

    @Bean
    public SimpleTriggerFactoryBean expireSSOSessionsTrigger(
            @Qualifier("expireSSOSessionsJobDetail") JobDetail jobDetail) {

        return createTrigger(jobDetail, 60_000); // 1 minute
    }

    @Bean
    public SimpleTriggerFactoryBean expireTokensTrigger(
            @Qualifier("expireTokensJobDetail") JobDetail jobDetail) {

        return createTrigger(jobDetail, 60_000); // 1 minute
    }
    // endregion

    // region Schedulers
    @Bean
    public Trigger[] triggers(
            @Qualifier("expireSessionsTrigger") Trigger expireSessionsTrigger,
            @Qualifier("expirePasswordsTrigger") Trigger expirePasswordsTrigger,
            @Qualifier("suspendUsersTrigger") Trigger suspendUsersTrigger,
            @Qualifier("expireSSOSessionsTrigger") Trigger expireSSOSessionsTrigger,
            @Qualifier("expireTokensTrigger") Trigger expireTokensTrigger
    ) {
        return new Trigger[]{
                expireSessionsTrigger,
                expirePasswordsTrigger,
                suspendUsersTrigger,
                expireSSOSessionsTrigger,
                expireTokensTrigger
        };
    }
//
//    @Bean
//    public NotificationSendStrategy sendStrategy(HttpClient httpClient) {
//        SimpleSendStrategy sendStrategy = new SimpleSendStrategy();
//        sendStrategy.setHttpClient(httpClient);
//        return sendStrategy;
//    }
//
//    @Bean
//    public SchedulerFactoryBean scheduler(Trigger[] triggers) {
//        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
//        scheduler.setTriggers(triggers);
//        return scheduler;
//    }

    @Bean
    public SchedulerFactoryBean persistentScheduler(Trigger[] triggers) {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setApplicationContextSchedulerContextKey("applicationContext");
        scheduler.setTriggers(triggers);
        return scheduler;
    }
    // endregion

    // region Helper Methods
    private MethodInvokingJobDetailFactoryBean createJobDetail(Object target, String method, int arg) {
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setTargetObject(target);
        jobDetail.setTargetMethod(method);
        jobDetail.setArguments(arg);
        return jobDetail;
    }

    private SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail, long interval) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(jobDetail);
        trigger.setRepeatInterval(interval);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        return trigger;
    }
    // endregion
}
