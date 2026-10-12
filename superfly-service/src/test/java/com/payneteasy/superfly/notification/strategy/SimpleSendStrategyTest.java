package com.payneteasy.superfly.notification.strategy;

import com.payneteasy.http.client.api.HttpRequest;
import com.payneteasy.http.client.api.HttpResponse;
import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.UsersChangedNotification;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.junit.Before;
import org.junit.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleSendStrategyTest {

    private static final long NOW = 1700000000000L;
    private static final String URI = "https://subsystem.example/superfly-callback";
    private static final String TOKEN = "dummy-subsystem-token";

    private final List<HttpRequest> sent = new ArrayList<>();
    private SubsystemDao subsystemDao;
    private SimpleSendStrategy strategy;

    @Before
    public void setUp() {
        subsystemDao = createMock(SubsystemDao.class);
        strategy = new SimpleSendStrategy() {
            @Override
            protected long currentTimeMillis() {
                return NOW;
            }
        };
        strategy.setHttpClient((request, parameters) -> {
            sent.add(request);
            return new HttpResponse(200, "OK", List.of(), new byte[0]);
        });
        strategy.setSubsystemDao(subsystemDao);
    }

    @Test
    public void logoutNotificationIsSignedWithTheStoredTokenHash() throws Exception {
        subsystems(subsystem("one", URI, SubsystemTokenHasher.hash(TOKEN)),
                subsystem("other", "https://other.example/cb", SubsystemTokenHasher.hash("other-token")));

        strategy.send(logout());

        Map<String, String[]> params = sentParams();
        assertEquals("LOGOUT", params.get("superflyNotification")[0]);
        assertEquals("1,2", params.get("superflyLogoutSessionIds")[0]);
        assertEquals(String.valueOf(NOW), params.get(NotificationSignatures.TIMESTAMP_PARAMETER)[0]);
        assertTrue(params.containsKey(NotificationSignatures.SIGNATURE_PARAMETER));
        assertEquals(NotificationSignatures.Result.VALID, NotificationSignatures.verify(
                SubsystemTokenHasher.hash(TOKEN), params, NOW, NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS));
    }

    @Test
    public void usersChangedNotificationIsSigned() throws Exception {
        subsystems(subsystem("one", URI, SubsystemTokenHasher.hash(TOKEN)));
        UsersChangedNotification notification = new UsersChangedNotification();
        notification.setCallbackUri(URI);

        strategy.send(notification);

        Map<String, String[]> params = sentParams();
        assertEquals("USERS_CHANGED", params.get("superflyNotification")[0]);
        assertEquals(NotificationSignatures.Result.VALID, NotificationSignatures.verify(
                SubsystemTokenHasher.hash(TOKEN), params, NOW, NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS));
    }

    @Test
    public void subsystemsSharingTheCallbackWithTheSameTokenAreNotified() throws Exception {
        subsystems(subsystem("one", URI, SubsystemTokenHasher.hash(TOKEN)),
                subsystem("two", URI, SubsystemTokenHasher.hash(TOKEN)));

        strategy.send(logout());

        assertEquals(1, sent.size());
    }

    @Test
    public void notificationIsNotSentWhenSubsystemHasNoToken() throws Exception {
        subsystems(subsystem("one", URI, null));

        strategy.send(logout());

        assertEquals(0, sent.size());
    }

    @Test
    public void notificationIsNotSentWhenStoredTokenIsNotHashed() throws Exception {
        subsystems(subsystem("one", URI, TOKEN));

        strategy.send(logout());

        assertEquals(0, sent.size());
    }

    @Test
    public void notificationIsNotSentWhenNoSubsystemHasTheCallback() throws Exception {
        subsystems(subsystem("other", "https://other.example/cb", SubsystemTokenHasher.hash(TOKEN)));

        strategy.send(logout());

        assertEquals(0, sent.size());
    }

    @Test
    public void notificationIsNotSentWhenSubsystemsSharingTheCallbackHaveDifferentTokens() throws Exception {
        subsystems(subsystem("one", URI, SubsystemTokenHasher.hash(TOKEN)),
                subsystem("two", URI, SubsystemTokenHasher.hash("other-token")));

        strategy.send(logout());

        assertEquals(0, sent.size());
    }

    @Test
    public void notificationIsNotSentWhenTokenLookupFails() throws Exception {
        expect(subsystemDao.getSubsystems()).andThrow(new IllegalStateException("db is down"));
        replay(subsystemDao);

        strategy.send(logout());

        assertEquals(0, sent.size());
    }

    private void subsystems(Object[]... subsystems) {
        List<UISubsystemForList> list = new ArrayList<>();
        for (Object[] subsystem : subsystems) {
            list.add((UISubsystemForList) subsystem[0]);
            if (URI.equals(((UISubsystemForList) subsystem[0]).getCallbackInformation())) {
                expect(subsystemDao.getSubsystemAuth(((UISubsystemForList) subsystem[0]).getName()))
                        .andReturn((SubsystemAuth) subsystem[1]).anyTimes();
            }
        }
        expect(subsystemDao.getSubsystems()).andReturn(list);
        replay(subsystemDao);
    }

    private static Object[] subsystem(String name, String callback, String storedToken) {
        UISubsystemForList forList = new UISubsystemForList();
        forList.setName(name);
        forList.setCallbackInformation(callback);
        SubsystemAuth auth = new SubsystemAuth();
        auth.setName(name);
        auth.setSubsystemToken(storedToken);
        return new Object[]{forList, auth};
    }

    private static LogoutNotification logout() {
        LogoutNotification notification = new LogoutNotification();
        notification.setCallbackUri(URI);
        notification.setSessionIds(Arrays.asList("1", "2"));
        return notification;
    }

    private Map<String, String[]> sentParams() {
        assertEquals(1, sent.size());
        assertEquals(URI, sent.get(0).getUrl());
        Map<String, String[]> params = new LinkedHashMap<>();
        for (String pair : new String(sent.get(0).getBody(), StandardCharsets.UTF_8).split("&")) {
            String[] nameValue = pair.split("=", 2);
            params.put(URLDecoder.decode(nameValue[0], StandardCharsets.UTF_8),
                    new String[]{URLDecoder.decode(nameValue[1], StandardCharsets.UTF_8)});
        }
        return params;
    }
}
