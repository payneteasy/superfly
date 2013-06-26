package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SessionDao;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.service.LoggerSink;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rpuch
 */
public class SessionServiceImplTest {
    private SessionDao sessionDao;
    private SessionServiceImpl sessionService;

    @Before
    public void setUp() {
        sessionDao = EasyMock.createStrictMock(SessionDao.class);
        sessionService = new SessionServiceImpl();
        sessionService.setSessionDao(sessionDao);
        sessionService.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
    }

    @Test
    public void testGetValidSSOSession() throws Exception {
        EasyMock.expect(sessionDao.getValidSSOSession("no-such-session")).andReturn(null);
        EasyMock.replay(sessionDao);

        assertEquals(null, sessionService.getValidSSOSession("no-such-session"));
        EasyMock.verify(sessionDao);

        EasyMock.reset(sessionDao);

        EasyMock.expect(sessionDao.getValidSSOSession("existing-session"))
                .andReturn(new SSOSession(1, "existing-session"));
        EasyMock.replay(sessionDao);

        SSOSession session = sessionService.getValidSSOSession("existing-session");
        Assert.assertNotNull(session);
        assertEquals(1, session.getId());
        EasyMock.verify(sessionDao);
    }

    @Test
    public void testCreateSSOSession() {
        EasyMock.expect(sessionDao.createSSOSession(EasyMock.eq("pete"), EasyMock.anyObject(String.class)))
                .andReturn(new SSOSession(1, "pete-session-id"));
        EasyMock.replay(sessionDao);

        SSOSession session = sessionService.createSSOSession("pete");
        assertEquals(1L, session.getId());
        assertEquals("pete-session-id", session.getIdentifier());

        EasyMock.verify(sessionDao);
    }

    @Test
    public void testDeleteExpiredSSOSessions() {
        sessionDao.deleteExpiredSSOSessions(10);
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteExpiredSSOSessions(10);

        EasyMock.verify(sessionDao);
    }

    @Test
    public void testDeleteExpiredTokens() {
        sessionDao.deleteExpiredTokens(5);
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteExpiredTokens(5);

        EasyMock.verify(sessionDao);
    }

    @Test
    public void testDeleteSSOSession() {
        sessionDao.deleteSSOSession("session-id");
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteSSOSession("session-id");

        EasyMock.verify(sessionDao);
    }
}
