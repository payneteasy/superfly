package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.common.notification.NotificationSignatures;
import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import com.payneteasy.superfly.common.session.SessionMapping;
import com.payneteasy.superfly.common.utils.SubsystemTokenHashes;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

/**
 * Logout notifications are processed only when signed with the hash of the configured subsystem token.
 */
public class SignedNotificationFiltersTest {

    private static final String TOKEN = "dummy-subsystem-token";
    private static final String REMOTE_ADDR = "127.0.0.1";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @SuppressWarnings("unchecked")
    private final SessionMapping<HttpSessionWrapper> sessionMapping = createMock(SessionMapping.class);
    private final HttpSessionWrapper session = createMock(HttpSessionWrapper.class);
    private final LogoutService logoutService = createMock(LogoutService.class);
    private HttpServletResponse response;
    private FilterChain chain;

    @Before
    public void setUp() {
        response = createMock(HttpServletResponse.class);
        chain = createMock(FilterChain.class);
    }

    @Test
    public void logoutFilterProcessesSignedNotification() throws Exception {
        SuperflyLogoutFilter filter = new SuperflyLogoutFilter(logoutService);
        filter.init(filterConfig(TOKEN, null));
        expect(logoutService.handleLogout("1,2")).andReturn(true);
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void logoutFilterAcceptsTokenFromSetter() throws Exception {
        SuperflyLogoutFilter filter = new SuperflyLogoutFilter(logoutService);
        filter.setNotificationSecret(TOKEN);
        expect(logoutService.handleLogout("1,2")).andReturn(true);
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void logoutFilterRejectsBadNotifications() throws Exception {
        for (Map<String, String[]> params : badNotifications()) {
            assertRejected(configured(new SuperflyLogoutFilter(logoutService)), params);
        }
    }

    @Test
    public void logoutFilterRejectsSignedNotificationWithoutConfiguredToken() throws Exception {
        assertRejected(unconfigured(new SuperflyLogoutFilter(logoutService)),
                signedParams(SubsystemTokenHashes.hash(TOKEN), now()));
    }

    @Test
    public void notificationSinkFilterProcessesSignedNotification() throws Exception {
        Filter filter = configured(notificationSinkFilter());
        expectSessionsInvalidated();
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void notificationSinkFilterRejectsBadNotifications() throws Exception {
        for (Map<String, String[]> params : badNotifications()) {
            assertRejected(configured(notificationSinkFilter()), params);
        }
        assertRejected(unconfigured(notificationSinkFilter()), signedParams(SubsystemTokenHashes.hash(TOKEN), now()));
    }

    @Test
    public void logoutNotificationSinkFilterProcessesSignedNotification() throws Exception {
        Filter filter = configured(logoutNotificationSinkFilter());
        expectSessionsInvalidated();
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void logoutNotificationSinkFilterRejectsBadNotifications() throws Exception {
        for (Map<String, String[]> params : badNotifications()) {
            assertRejected(configured(logoutNotificationSinkFilter()), params);
        }
        assertRejected(unconfigured(logoutNotificationSinkFilter()),
                signedParams(SubsystemTokenHashes.hash(TOKEN), now()));
    }

    @Test
    public void logoutNotificationSinkFilterTakesTokenFromSetter() throws Exception {
        LogoutNotificationSinkFilter filter = logoutNotificationSinkFilter();
        filter.setNotificationSecret(TOKEN);
        expectSessionsInvalidated();
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void protectedFilterProcessesSignedNotificationFromAllowedIp() throws Exception {
        Filter filter = protectedFilter(TOKEN, REMOTE_ADDR);
        expectSessionsInvalidated();
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void protectedFilterStillChecksIp() throws Exception {
        Filter filter = protectedFilter(TOKEN, "10.0.0.1");
        chain.doFilter(anyObject(), anyObject());
        expectLastCall();
        replayAll();

        filter.doFilter(request(signedParams(SubsystemTokenHashes.hash(TOKEN), now())), response, chain);

        verifyAll();
    }

    @Test
    public void protectedFilterRejectsBadNotificationsFromAllowedIp() throws Exception {
        for (Map<String, String[]> params : badNotifications()) {
            assertRejected(protectedFilter(TOKEN, REMOTE_ADDR), params);
        }
        assertRejected(protectedFilter(null, REMOTE_ADDR), signedParams(SubsystemTokenHashes.hash(TOKEN), now()));
    }

    private Filter protectedFilter(String token, String allowedIps) throws Exception {
        File properties = folder.newFile();
        String content = "notification.allowed.ips=" + allowedIps + "\n"
                + (token == null ? "" : "notification.secret=" + token + "\n");
        Files.write(properties.toPath(), content.getBytes(StandardCharsets.ISO_8859_1));
        DefaultProtectedLogoutNotificationSinkFilter filter = new DefaultProtectedLogoutNotificationSinkFilter() {
            @Override
            protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
                return sessionMapping;
            }
        };
        filter.init(filterConfig(null, properties.getAbsolutePath()));
        return filter;
    }

    private void assertRejected(Filter filter, Map<String, String[]> params) throws Exception {
        reset(sessionMapping, session, logoutService);
        response = createMock(HttpServletResponse.class);
        chain = createMock(FilterChain.class);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        expectLastCall();
        replayAll();

        filter.doFilter(request(params), response, chain);

        verifyAll();
    }

    private Filter configured(Filter filter) throws Exception {
        filter.init(filterConfig(TOKEN, null));
        return filter;
    }

    private Filter unconfigured(Filter filter) throws Exception {
        filter.init(filterConfig(null, null));
        return filter;
    }

    private static Iterable<Map<String, String[]>> badNotifications() {
        Map<String, String[]> tampered = signedParams(SubsystemTokenHashes.hash(TOKEN), now());
        tampered.put("superflyLogoutSessionIds", new String[]{"1,2,3"});
        return List.of(
                params(),
                signedParams(SubsystemTokenHashes.hash("other-token"), now()),
                signedParams(TOKEN, now()),
                signedParams(SubsystemTokenHashes.hash(TOKEN),
                        now() - NotificationSignatures.DEFAULT_MAX_CLOCK_SKEW_MILLIS - 60_000),
                tampered);
    }

    private SuperflyNotificationSinkFilter notificationSinkFilter() {
        return new SuperflyNotificationSinkFilter() {
            @Override
            protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
                return sessionMapping;
            }
        };
    }

    private LogoutNotificationSinkFilter logoutNotificationSinkFilter() {
        return new LogoutNotificationSinkFilter() {
            @Override
            protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
                return sessionMapping;
            }
        };
    }

    private void expectSessionsInvalidated() {
        expect(sessionMapping.removeSessionByKey("1")).andReturn(session);
        expect(sessionMapping.removeSessionByKey("2")).andReturn(null);
        session.invalidate();
        expectLastCall();
    }

    private void replayAll() {
        replay(sessionMapping, session, logoutService, response, chain);
    }

    private void verifyAll() {
        verify(sessionMapping, session, logoutService, response, chain);
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static Map<String, String[]> params() {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put("superflyNotification", new String[]{"LOGOUT"});
        params.put("superflyLogoutSessionIds", new String[]{"1,2"});
        return params;
    }

    private static Map<String, String[]> signedParams(String key, long timestamp) {
        Map<String, String[]> params = params();
        params.put(NotificationSignatures.TIMESTAMP_PARAMETER, new String[]{String.valueOf(timestamp)});
        params.put(NotificationSignatures.SIGNATURE_PARAMETER, new String[]{NotificationSignatures.sign(key, params)});
        return params;
    }

    private static HttpServletRequest request(Map<String, String[]> params) {
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getMethod()).andReturn("POST").anyTimes();
        expect(request.getRemoteAddr()).andReturn(REMOTE_ADDR).anyTimes();
        expect(request.getParameterMap()).andReturn(params).anyTimes();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            expect(request.getParameter(entry.getKey())).andReturn(entry.getValue()[0]).anyTimes();
        }
        expect(request.getParameter(anyString())).andReturn(null).anyTimes();
        replay(request);
        return request;
    }

    private static FilterConfig filterConfig(String token, String propertiesResource) {
        FilterConfig config = createNiceMock(FilterConfig.class);
        expect(config.getInitParameter("notificationSecret")).andReturn(token).anyTimes();
        expect(config.getInitParameter("propertiesResource")).andReturn(propertiesResource).anyTimes();
        replay(config);
        return config;
    }
}
