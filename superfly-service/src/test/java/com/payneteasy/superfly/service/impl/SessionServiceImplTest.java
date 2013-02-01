package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SessionDao;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.service.LoggerSink;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @author rpuch
 */
public class SessionServiceImplTest extends TestCase {
    private SessionDao sessionDao;
    private SessionServiceImpl sessionService;

    public void setUp() {
        sessionDao = EasyMock.createStrictMock(SessionDao.class);
        sessionService = new SessionServiceImpl();
        sessionService.setSessionDao(sessionDao);
        sessionService.setLoggerSink(TrivialProxyFactory.createProxy(LoggerSink.class));
    }

    public void testGetValidSSOSession() throws Exception {
        EasyMock.expect(sessionDao.getValidSSOSession("no-such-session")).andReturn(null);
        EasyMock.replay(sessionDao);

        Assert.assertEquals(null, sessionService.getValidSSOSession("no-such-session"));
        EasyMock.verify(sessionDao);

        EasyMock.reset(sessionDao);

        EasyMock.expect(sessionDao.getValidSSOSession("existing-session"))
                .andReturn(new SSOSession(1, "existing-session"));
        EasyMock.replay(sessionDao);

        SSOSession session = sessionService.getValidSSOSession("existing-session");
        Assert.assertNotNull(session);
        Assert.assertEquals(1, session.getId());
        EasyMock.verify(sessionDao);
    }

    public void testCreateSSOSession() {
        EasyMock.expect(sessionDao.createSSOSession(EasyMock.eq("pete"), EasyMock.anyObject(String.class)))
                .andReturn(new SSOSession(1, "pete-session-id"));
        EasyMock.replay(sessionDao);

        SSOSession session = sessionService.createSSOSession("pete");
        Assert.assertEquals(1L, session.getId());
        Assert.assertEquals("pete-session-id", session.getIdentifier());

        EasyMock.verify(sessionDao);
    }

    public void testDeleteExpiredSSOSessions() {
        sessionDao.deleteExpiredSSOSessions(10);
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteExpiredSSOSessions(10);

        EasyMock.verify(sessionDao);
    }

    public void testDeleteExpiredTokens() {
        sessionDao.deleteExpiredTokens(5);
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteExpiredTokens(5);

        EasyMock.verify(sessionDao);
    }

    public void testDeleteSSOSession() {
        sessionDao.deleteSSOSession("session-id");
        EasyMock.expectLastCall();
        EasyMock.replay(sessionDao);

        sessionService.deleteSSOSession("session-id");

        EasyMock.verify(sessionDao);
    }
}
