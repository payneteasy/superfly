package com.payneteasy.superfly.notification.strategy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Every line the send strategy writes about a callback URL (and the message of the exception it throws) names the
 * URL without user info, query and fragment.
 */
public class SimpleSendStrategyLogMaskingTest {

    private static final String TOKEN = "dummy-subsystem-token";
    private static final String CALLBACK = "http://callback-user:dummy-password@superfly_client:8080/superfly-callback"
            + "?key=dummy-query-secret#dummy-fragment";
    private static final String MASKED = "http://superfly_client:8080/superfly-callback";
    private static final List<String> HIDDEN = List.of("callback-user", "dummy-password", "dummy-query-secret",
            "dummy-fragment");

    private final Logger strategyLogger = (Logger) LoggerFactory.getLogger(AbstractHttpNotificationSendStrategy.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final List<HttpRequest> sent = new ArrayList<>();
    private Level originalLevel;
    private int answerStatus = 200;
    private SimpleSendStrategy strategy;

    @Before
    public void setUp() {
        strategy = new SimpleSendStrategy();
        strategy.setHttpClient((request, parameters) -> {
            sent.add(request);
            return new HttpResponse(answerStatus, "answer", List.of(), new byte[0]);
        });
        originalLevel = strategyLogger.getLevel();
        strategyLogger.setLevel(Level.DEBUG);
        appender.start();
        strategyLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        strategyLogger.detachAppender(appender);
        strategyLogger.setLevel(originalLevel);
    }

    @Test
    public void successfulNotificationIsLoggedWithTheMaskedUrl() throws Exception {
        subsystems(new Subsystem("one", CALLBACK, SubsystemTokenHasher.hash(TOKEN)));

        strategy.send(usersChanged(CALLBACK));

        assertEquals(1, sent.size());
        assertEquals(CALLBACK, sent.get(0).getUrl());
        assertTrue(lines(Level.DEBUG).toString(), lines(Level.DEBUG).stream()
                .anyMatch(line -> line.startsWith("Sending notification to " + MASKED + " with params")));
        assertTrue(lines(Level.INFO).toString(), lines(Level.INFO).stream()
                .anyMatch(line -> line.startsWith("Successfully notified " + MASKED + " with params")));
        assertNothingHidden();
    }

    @Test
    public void failedNotificationIsReportedWithTheMaskedUrl() {
        subsystems(new Subsystem("one", CALLBACK, SubsystemTokenHasher.hash(TOKEN)));
        answerStatus = 500;

        List<String> reported = new ArrayList<>();
        try {
            strategy.send(usersChanged(CALLBACK));
            fail("status 500 must fail the notification");
        } catch (NotificationException e) {
            reported.add(e.getMessage());
        }
        reported.addAll(lines(Level.WARN));

        assertEquals(reported.toString(), 2, reported.size());
        for (String text : reported) {
            assertTrue(text, text.contains("to " + MASKED + " failed"));
            assertHidden(text);
        }
        assertNothingHidden();
    }

    @Test
    public void subsystemWithoutTokenIsReportedWithTheMaskedUrl() throws Exception {
        subsystems(new Subsystem("one", CALLBACK, null));

        strategy.send(usersChanged(CALLBACK));

        assertEquals(0, sent.size());
        assertSingleLine(Level.WARN, "Subsystem one has no token, not sending a notification to " + MASKED);
        assertNothingHidden();
    }

    @Test
    public void callbackOfNoSubsystemIsReportedWithTheMaskedUrl() throws Exception {
        subsystems(new Subsystem("other", "https://other.example/cb", SubsystemTokenHasher.hash(TOKEN)));

        strategy.send(usersChanged(CALLBACK));

        assertEquals(0, sent.size());
        assertSingleLine(Level.WARN, "No subsystem tokens for callback " + MASKED + ", not sending a notification");
        assertNothingHidden();
    }

    @Test
    public void differentTokensOfOneCallbackAreReportedWithTheMaskedUrl() throws Exception {
        subsystems(new Subsystem("one", CALLBACK, SubsystemTokenHasher.hash(TOKEN)),
                new Subsystem("two", CALLBACK, SubsystemTokenHasher.hash("other-token")));

        strategy.send(usersChanged(CALLBACK));

        assertEquals(0, sent.size());
        assertSingleLine(Level.WARN, "Different subsystem tokens for callback " + MASKED
                + ", not sending a notification");
        assertNothingHidden();
    }

    @Test
    public void failedTokenLookupIsReportedWithTheMaskedUrl() throws Exception {
        SubsystemDao subsystemDao = createMock(SubsystemDao.class);
        expect(subsystemDao.getSubsystems()).andThrow(new IllegalStateException("db is down"));
        replay(subsystemDao);
        strategy.setSubsystemDao(subsystemDao);

        strategy.send(usersChanged(CALLBACK));

        assertEquals(0, sent.size());
        assertSingleLine(Level.ERROR, "Could not obtain the subsystem token for " + MASKED
                + ", not sending a notification");
        assertNothingHidden();
    }

    private void assertSingleLine(Level level, String expected) {
        List<String> lines = lines(level);
        assertEquals(lines.toString(), List.of(expected), lines);
    }

    private void assertNothingHidden() {
        for (ILoggingEvent event : appender.list) {
            assertHidden(event.getFormattedMessage());
        }
    }

    private static void assertHidden(String text) {
        for (String hidden : HIDDEN) {
            assertFalse(text, text.contains(hidden));
        }
    }

    private List<String> lines(Level level) {
        return appender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    private void subsystems(Subsystem... subsystems) {
        List<UISubsystemForList> list = new ArrayList<>();
        SubsystemDao subsystemDao = createMock(SubsystemDao.class);
        for (Subsystem subsystem : subsystems) {
            UISubsystemForList forList = new UISubsystemForList();
            forList.setName(subsystem.name());
            forList.setCallbackInformation(subsystem.callback());
            list.add(forList);
            SubsystemAuth auth = new SubsystemAuth();
            auth.setName(subsystem.name());
            auth.setSubsystemToken(subsystem.storedToken());
            expect(subsystemDao.getSubsystemAuth(subsystem.name())).andReturn(auth).anyTimes();
        }
        expect(subsystemDao.getSubsystems()).andReturn(list).anyTimes();
        replay(subsystemDao);
        strategy.setSubsystemDao(subsystemDao);
    }

    private static UsersChangedNotification usersChanged(String callback) {
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(callback);
        return notification;
    }

    private record Subsystem(String name, String callback, String storedToken) {
    }
}
