package com.payneteasy.superfly.notification.strategy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.NotificationException;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
 * A notification is delivered only if the subsystem answers with a 2xx status: any other answer is logged as
 * a warning and fails the notification the same way a transport error does.
 */
public class SimpleSendStrategyAnswerStatusTest {

    private static final long NOW = 1700000000000L;
    private static final String CALLBACK = "https://subsystem.example/superfly-callback";
    private static final String TOKEN = "dummy-subsystem-token";
    private static final String HINT = "check notificationSecret of the subsystem client";

    private final Logger strategyLogger = (Logger) LoggerFactory.getLogger(AbstractHttpNotificationSendStrategy.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final List<HttpRequest> sent = new ArrayList<>();
    private int answerStatus = 200;
    private SimpleSendStrategy strategy;

    @Before
    public void setUp() {
        strategy = new SimpleSendStrategy() {
            @Override
            protected long currentTimeMillis() {
                return NOW;
            }
        };
        strategy.setHttpClient((request, parameters) -> {
            sent.add(request);
            return new HttpResponse(answerStatus, "answer", List.of(), new byte[0]);
        });
        appender.start();
        strategyLogger.addAppender(appender);
    }

    @After
    public void tearDown() {
        strategyLogger.detachAppender(appender);
    }

    @Test
    public void answerWith2xxIsASuccess() throws Exception {
        subsystemWithCallback(CALLBACK);
        for (int status : new int[]{200, 204, 299}) {
            appender.list.clear();
            answerStatus = status;

            strategy.send(usersChanged(CALLBACK));

            List<String> info = messages(Level.INFO);
            assertEquals(info.toString(), 1, info.size());
            assertTrue(info.get(0), info.get(0).contains("Successfully notified " + CALLBACK));
            assertTrue(info.get(0), info.get(0).contains("(status " + status + ")"));
            assertEquals(List.of(), messages(Level.WARN));
            for (String secret : secrets()) {
                assertFalse(info.get(0), info.get(0).contains(secret));
            }
        }
    }

    @Test
    public void answerWithRedirectFailsTheNotification() {
        String warning = assertFails(302);

        assertFalse(warning, warning.contains(HINT));
    }

    @Test
    public void answerWithForbiddenFailsTheNotificationAndHintsAtTheNotificationSecret() {
        String warning = assertFails(403);

        assertTrue(warning, warning.contains(HINT));
    }

    @Test
    public void answerWithServerErrorFailsTheNotification() {
        String warning = assertFails(500);

        assertFalse(warning, warning.contains(HINT));
    }

    @Test
    public void answerJustOutside2xxFailsTheNotification() {
        assertFails(300);
    }

    @Test
    public void failedLogoutNotificationNamesItsType() {
        subsystemWithCallback(CALLBACK);
        answerStatus = 500;

        try {
            strategy.send(logout(CALLBACK));
            fail("status 500 must fail the notification");
        } catch (NotificationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("LOGOUT"));
        }

        List<String> warnings = messages(Level.WARN);
        assertEquals(warnings.toString(), 1, warnings.size());
        assertTrue(warnings.get(0), warnings.get(0).contains("LOGOUT"));
    }

    @Test
    public void failureReportsNeitherSecretsNorCredentialsOfTheCallbackUrl() {
        String callback = "https://callback-user:dummy-password@subsystem.example:8443/superfly-callback"
                + "?key=dummy-query-secret#dummy-fragment";
        subsystemWithCallback(callback);
        answerStatus = 403;

        List<String> reported = new ArrayList<>();
        try {
            strategy.send(usersChanged(callback));
            fail("status 403 must fail the notification");
        } catch (NotificationException e) {
            reported.add(e.getMessage());
        }
        reported.addAll(messages(Level.WARN));

        assertEquals(reported.toString(), 2, reported.size());
        List<String> hidden = new ArrayList<>(secrets());
        hidden.addAll(List.of("callback-user", "dummy-password", "dummy-query-secret", "dummy-fragment"));
        for (String text : reported) {
            assertTrue(text, text.contains("https://subsystem.example:8443/superfly-callback"));
            for (String secret : hidden) {
                assertFalse(text, text.contains(secret));
            }
        }
    }

    @Test
    public void failureDoesNotRepeatACallbackUrlThatCannotBeParsed() {
        String callback = "https://callback-user:dummy-password@subsystem.example/superfly callback?key=dummy-query-secret";
        subsystemWithCallback(callback);
        answerStatus = 500;

        List<String> reported = new ArrayList<>();
        try {
            strategy.send(usersChanged(callback));
            fail("status 500 must fail the notification");
        } catch (NotificationException e) {
            reported.add(e.getMessage());
        }
        reported.addAll(messages(Level.WARN));

        assertEquals(reported.toString(), 2, reported.size());
        for (String text : reported) {
            assertTrue(text, text.contains("USERS_CHANGED") && text.contains("status 500"));
            assertFalse(text, text.contains("dummy-password") || text.contains("dummy-query-secret"));
        }
    }

    /** @return the warning logged for the failed notification */
    private String assertFails(int status) {
        subsystemWithCallback(CALLBACK);
        answerStatus = status;

        try {
            strategy.send(usersChanged(CALLBACK));
            fail("status " + status + " must fail the notification");
        } catch (NotificationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("USERS_CHANGED"));
            assertTrue(e.getMessage(), e.getMessage().contains(CALLBACK));
            assertTrue(e.getMessage(), e.getMessage().contains("status " + status));
        }

        assertEquals(1, sent.size());
        assertEquals(List.of(), messages(Level.INFO));
        List<String> warnings = messages(Level.WARN);
        assertEquals(warnings.toString(), 1, warnings.size());
        String warning = warnings.get(0);
        assertTrue(warning, warning.contains("USERS_CHANGED"));
        assertTrue(warning, warning.contains(CALLBACK));
        assertTrue(warning, warning.contains("status " + status));
        return warning;
    }

    /** The signature of the last request sent, the stored token hash the request is signed with, and the token. */
    private List<String> secrets() {
        String body = new String(sent.get(sent.size() - 1).getBody(), StandardCharsets.UTF_8);
        String prefix = NotificationSignatures.SIGNATURE_PARAMETER + "=";
        String signature = null;
        for (String pair : body.split("&")) {
            if (pair.startsWith(prefix)) {
                signature = URLDecoder.decode(pair.substring(prefix.length()), StandardCharsets.UTF_8);
            }
        }
        assertTrue("the request must be signed", signature != null && !signature.isEmpty());
        return List.of(signature, SubsystemTokenHasher.hash(TOKEN), TOKEN);
    }

    private List<String> messages(Level level) {
        return appender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
    }

    private void subsystemWithCallback(String callback) {
        UISubsystemForList subsystem = new UISubsystemForList();
        subsystem.setName("one");
        subsystem.setCallbackInformation(callback);
        SubsystemAuth auth = new SubsystemAuth();
        auth.setName("one");
        auth.setSubsystemToken(SubsystemTokenHasher.hash(TOKEN));
        SubsystemDao subsystemDao = createMock(SubsystemDao.class);
        expect(subsystemDao.getSubsystems()).andReturn(List.of(subsystem)).anyTimes();
        expect(subsystemDao.getSubsystemAuth("one")).andReturn(auth).anyTimes();
        replay(subsystemDao);
        strategy.setSubsystemDao(subsystemDao);
    }

    private static UsersChangedNotification usersChanged(String callback) {
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(callback);
        return notification;
    }

    private static LogoutNotification logout(String callback) {
        LogoutNotification notification = new LogoutNotification();
        notification.setCallbackUri(callback);
        notification.setSessionIds(List.of("1", "2"));
        return notification;
    }
}
