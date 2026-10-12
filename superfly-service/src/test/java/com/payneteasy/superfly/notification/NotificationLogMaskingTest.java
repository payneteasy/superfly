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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A notification names its callback URL without user info, query and fragment, so does every line the notifier
 * writes about it.
 */
public class NotificationLogMaskingTest {

    private static final String CALLBACK = "http://callback-user:dummy-password@superfly_client:8080/superfly-callback"
            + "?key=dummy-query-secret#dummy-fragment";
    private static final String MASKED = "http://superfly_client:8080/superfly-callback";
    private static final List<String> HIDDEN = List.of("callback-user", "dummy-password", "dummy-query-secret",
            "dummy-fragment");

    private final Logger notifierLogger = (Logger) LoggerFactory.getLogger(DefaultNotifier.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Capture<Runnable> scheduled = Capture.newInstance();
    private Level originalLevel;
    private TaskScheduler taskScheduler;
    private NotificationSendStrategy sendStrategy;
    private DefaultNotifier notifier;

    @Before
    public void setUp() {
        taskScheduler = createMock(TaskScheduler.class);
        expect(taskScheduler.schedule(capture(scheduled), anyObject(Instant.class))).andReturn(null);
        sendStrategy = createMock(NotificationSendStrategy.class);
        notifier = new DefaultNotifier(taskScheduler, sendStrategy);
        originalLevel = notifierLogger.getLevel();
        notifierLogger.setLevel(Level.DEBUG);
        appender.start();
        notifierLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        notifierLogger.detachAppender(appender);
        notifierLogger.setLevel(originalLevel);
    }

    @Test
    public void toStringOfNotificationsNamesTheMaskedUrl() {
        assertEquals("UsersChangedNotification [for " + MASKED + "]", usersChanged().toString());
        assertEquals("LogoutNotification [for " + MASKED + "]: 1,2", logout().toString());
    }

    @Test
    public void toStringOfNotificationWithoutUrlNamesNull() {
        assertEquals("UsersChangedNotification [for null]", new UsersChangedNotification().toString());
    }

    @Test
    public void linesAboutNotificationDispatchedAndFailedWithUnexpectedErrorNameTheMaskedUrl() throws Exception {
        LogoutNotification notification = logout();
        sendStrategy.send(notification);
        expectLastCall().andThrow(new IllegalStateException("dummy failure"));
        replay(taskScheduler, sendStrategy);

        notifier.notifyAboutLogout(List.of(notification));
        scheduled.getValue().run();

        assertEquals(lines(Level.INFO).toString(), 1, lines(Level.INFO).size());
        assertTrue(lines(Level.INFO).get(0), lines(Level.INFO).get(0).contains("[for " + MASKED + "]"));
        assertEquals(lines(Level.DEBUG).toString(), 2, lines(Level.DEBUG).size());
        for (String line : lines(Level.DEBUG)) {
            assertTrue(line, line.contains("[for " + MASKED + "]"));
        }
        assertEquals(lines(Level.ERROR).toString(), 1, lines(Level.ERROR).size());
        assertTrue(lines(Level.ERROR).get(0), lines(Level.ERROR).get(0).contains("[for " + MASKED + "]"));
        for (ILoggingEvent event : appender.list) {
            for (String hidden : HIDDEN) {
                assertFalse(event.getFormattedMessage(), event.getFormattedMessage().contains(hidden));
            }
        }
    }

    private List<String> lines(Level level) {
        return appender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    private static UsersChangedNotification usersChanged() {
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(CALLBACK);
        return notification;
    }

    private static LogoutNotification logout() {
        LogoutNotification notification = new LogoutNotification();
        notification.setCallbackUri(CALLBACK);
        notification.setSessionIds(List.of("1", "2"));
        return notification;
    }
}
