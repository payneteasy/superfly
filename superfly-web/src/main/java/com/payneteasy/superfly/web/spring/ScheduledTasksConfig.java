package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Background maintenance tasks, previously driven by Quartz
 * ({@code MethodInvokingJobDetailFactoryBean} + {@code SimpleTrigger}).
 *
 * <p>Migrated to Spring {@link Scheduled} fixed-rate tasks backed by the shared
 * {@code taskScheduler} bean from {@link TaskSchedulerConfig}, so no Quartz
 * runtime is required anymore.
 *
 * <p>Intervals and arguments mirror the previous Quartz triggers exactly.
 */
@Configuration
public class ScheduledTasksConfig {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksConfig.class);

    /** Repeat intervals (ms), identical to the former Quartz SimpleTrigger values. */
    private static final long ONE_MINUTE_MS = 60_000L;
    private static final long ONE_DAY_MS    = 86_400_000L;

    /** Method arguments, identical to the former Quartz MethodInvokingJobDetailFactoryBean values. */
    private static final int EXPIRED_SESSION_AGE_SECONDS = 86_400;
    private static final int PASSWORD_EXPIRY_DAYS        = 90;
    private static final int SUSPEND_USERS_DAYS          = 90;
    private static final int EXPIRED_TOKEN_AGE_SECONDS   = 5;

    private final SessionService sessionService;
    private final UserService    userService;
    private final int            ssoSessionAge;

    public ScheduledTasksConfig(
            SessionService sessionService,
            UserService userService,
            @Value("#{contextParameters['superfly-max-sso-session-age-minutes']}") int ssoSessionAge) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.ssoSessionAge = ssoSessionAge;
        log.debug("ScheduledTasksConfig initialized (ssoSessionAge={})", ssoSessionAge);
    }

    @Scheduled(fixedRate = ONE_MINUTE_MS)
    public void expireSessions() {
        runQuietly("expireSessions",
                () -> sessionService.deleteExpiredAndOldSessionsAndNotify(EXPIRED_SESSION_AGE_SECONDS));
    }

    @Scheduled(fixedRate = ONE_DAY_MS)
    public void expirePasswords() {
        runQuietly("expirePasswords", () -> userService.expirePasswords(PASSWORD_EXPIRY_DAYS));
    }

    @Scheduled(fixedRate = ONE_DAY_MS)
    public void suspendUsers() {
        runQuietly("suspendUsers", () -> userService.suspendUsers(SUSPEND_USERS_DAYS));
    }

    @Scheduled(fixedRate = ONE_MINUTE_MS)
    public void expireSsoSessions() {
        runQuietly("expireSsoSessions", () -> sessionService.deleteExpiredSSOSessions(ssoSessionAge));
    }

    @Scheduled(fixedRate = ONE_MINUTE_MS)
    public void expireTokens() {
        runQuietly("expireTokens", () -> sessionService.deleteExpiredTokens(EXPIRED_TOKEN_AGE_SECONDS));
    }

    /**
     * Runs a maintenance task, logging entry/exit at DEBUG and swallowing
     * exceptions so a single failed run never kills the scheduler thread
     * (Quartz isolated job failures the same way).
     */
    private void runQuietly(String taskName, Runnable task) {
        log.debug("Scheduled task '{}' starting", taskName);
        try {
            task.run();
            log.debug("Scheduled task '{}' finished", taskName);
        } catch (Exception e) {
            log.error("Scheduled task '{}' failed", taskName, e);
        }
    }
}
