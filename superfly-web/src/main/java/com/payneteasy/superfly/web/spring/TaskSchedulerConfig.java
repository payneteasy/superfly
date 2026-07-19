package com.payneteasy.superfly.web.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Shared {@link TaskScheduler} used by {@code @Scheduled} maintenance tasks
 * ({@link ScheduledTasksConfig}) and by the notification dispatch path
 * ({@code DefaultNotifier}).
 *
 * <p>Kept dependency-free on purpose: the scheduler is injected into
 * {@code DefaultNotifier}, which sits on the {@code sessionService -> notifier}
 * wiring path. Defining the bean here (rather than on a service-dependent
 * config) avoids a startup circular reference. {@code @EnableScheduling} picks
 * up a bean named {@code taskScheduler} automatically.
 */
@Configuration
@EnableScheduling
public class TaskSchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(TaskSchedulerConfig.class);

    private static final int POOL_SIZE = 5;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("superfly-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        log.debug("Initialized ThreadPoolTaskScheduler (poolSize={})", POOL_SIZE);
        return scheduler;
    }
}
