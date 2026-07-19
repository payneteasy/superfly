package com.payneteasy.superfly.client.session;

import com.payneteasy.superfly.common.session.HttpSessionWrapper;
import com.payneteasy.superfly.common.session.SessionMapping;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LogoutServiceTest {

    private SessionMapping<HttpSessionWrapper> sessionMapping;
    private HttpSessionWrapper session1;
    private HttpSessionWrapper session2;
    private LogoutService logoutService;

    @Before
    public void setUp() {
        sessionMapping = EasyMock.createMock(SessionMapping.class);
        session1 = EasyMock.createMock(HttpSessionWrapper.class);
        session2 = EasyMock.createMock(HttpSessionWrapper.class);

        logoutService = new LogoutService() {
            @Override
            protected SessionMapping<HttpSessionWrapper> getSessionMapping() {
                return sessionMapping;
            }
        };
    }

    @Test
    public void handleLogout_WithValidSessionIds_ShouldReturnTrue() {
        String sessionIds = "session1,session2";
        EasyMock.expect(sessionMapping.removeSessionByKey("session1")).andReturn(session1);
        EasyMock.expect(sessionMapping.removeSessionByKey("session2")).andReturn(session2);
        session1.invalidate();
        session2.invalidate();

        EasyMock.replay(sessionMapping, session1, session2);

        boolean result = logoutService.handleLogout(sessionIds);

        Assert.assertTrue(result);

        EasyMock.verify(sessionMapping, session1, session2);
    }

    @Test
    public void handleLogout_WithInvalidSession_ShouldHandleException() {
        String sessionIds = "session1";
        EasyMock.expect(sessionMapping.removeSessionByKey("session1")).andReturn(session1);
        session1.invalidate();
        EasyMock.expectLastCall().andThrow(new IllegalStateException("Session is already invalid"));

        EasyMock.replay(sessionMapping, session1);

        boolean result = logoutService.handleLogout(sessionIds);

        Assert.assertTrue(result);

        EasyMock.verify(sessionMapping, session1);
    }

    @Test
    public void handleLogout_WithNullSessionIds_ShouldReturnFalse() {
        EasyMock.replay(sessionMapping);

        boolean result = logoutService.handleLogout(null);

        Assert.assertFalse(result);

        EasyMock.verify(sessionMapping);
    }

    @Test
    public void handleLogout_WithEmptySessionIds_ShouldReturnFalse() {
        EasyMock.replay(sessionMapping);

        boolean result = logoutService.handleLogout("  ");

        Assert.assertFalse(result);

        EasyMock.verify(sessionMapping);
    }
}
