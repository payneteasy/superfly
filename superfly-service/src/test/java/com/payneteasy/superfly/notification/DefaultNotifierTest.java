package com.payneteasy.superfly.notification;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.superfly.notification.strategy.NotificationSendStrategy;
import org.easymock.Capture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A notification is sent in a single attempt. A failed notification (the subsystem refused it or could not be
 * reached) is logged as one error line, any other failure is logged with its stack trace.
 */
public class DefaultNotifierTest {

    private static final String CALLBACK = "https://subsystem.example/superfly-callback";

    private final Logger notifierLogger = (Logger) LoggerFactory.getLogger(DefaultNotifier.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Capture<Runnable> scheduled = Capture.newInstance();
    private TaskScheduler taskScheduler;
    private NotificationSendStrategy sendStrategy;
    private DefaultNotifier notifier;

    @Before
    public void setUp() {
        taskScheduler = createMock(TaskScheduler.class);
        expect(taskScheduler.schedule(capture(scheduled), anyObject(Instant.class))).andReturn(null);
        sendStrategy = createMock(NotificationSendStrategy.class);
        notifier = new DefaultNotifier(taskScheduler, sendStrategy);
        appender.start();
        notifierLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        notifierLogger.detachAppender(appender);
    }

    @Test
    public void failedUsersChangedNotificationIsLoggedAsOneLineWithoutStackTrace() throws Exception {
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(CALLBACK);
        sendStrategy.send(notification);
        expectLastCall().andThrow(new NotificationException(
                "Notification USERS_CHANGED to " + CALLBACK + " failed: the subsystem answered with status 403"));
        replay(taskScheduler, sendStrategy);

        notifier.notifyAboutUsersChanged(List.of(notification));
        scheduled.getValue().run();

        ILoggingEvent error = onlyError();
        assertNull(error.getThrowableProxy());
        String message = error.getFormattedMessage();
        assertFalse(message, message.contains("\n"));
        assertTrue(message, message.contains("UsersChangedNotification"));
        assertTrue(message, message.contains("failed: the subsystem answered with status 403"));
        verify(sendStrategy);
    }

    @Test
    public void failedLogoutNotificationIsLoggedAsOneLineWithoutStackTrace() throws Exception {
        LogoutNotification notification = new LogoutNotification();
        notification.setCallbackUri(CALLBACK);
        notification.setSessionIds(List.of("1", "2"));
        sendStrategy.send(notification);
        expectLastCall().andThrow(new NotificationException(
                "Notification LOGOUT to " + CALLBACK + " failed: the subsystem answered with status 500"));
        replay(taskScheduler, sendStrategy);

        notifier.notifyAboutLogout(List.of(notification));
        scheduled.getValue().run();

        ILoggingEvent error = onlyError();
        assertNull(error.getThrowableProxy());
        String message = error.getFormattedMessage();
        assertFalse(message, message.contains("\n"));
        assertTrue(message, message.contains("LogoutNotification"));
        assertTrue(message, message.contains("failed: the subsystem answered with status 500"));
        verify(sendStrategy);
    }

    @Test
    public void unexpectedFailureIsLoggedWithStackTrace() throws Exception {
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(CALLBACK);
        sendStrategy.send(notification);
        expectLastCall().andThrow(new IllegalStateException("dummy failure"));
        replay(taskScheduler, sendStrategy);

        notifier.notifyAboutUsersChanged(List.of(notification));
        scheduled.getValue().run();

        ILoggingEvent error = onlyError();
        assertNotNull(error.getThrowableProxy());
        assertEquals(IllegalStateException.class.getName(), error.getThrowableProxy().getClassName());
        assertTrue(error.getFormattedMessage(), error.getFormattedMessage().contains(notification.toString()));
        verify(sendStrategy);
    }

    private ILoggingEvent onlyError() {
        List<ILoggingEvent> errors = appender.list.stream()
                .filter(event -> event.getLevel() == Level.ERROR)
                .collect(Collectors.toList());
        assertEquals(errors.toString(), 1, errors.size());
        return errors.get(0);
    }
}
