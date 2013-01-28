package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SessionDao;
import com.payneteasy.superfly.model.SSOSession;
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
    }

    public void testGetValidSSOSession() throws Exception {
        EasyMock.expect(sessionDao.getValidSSOSession("no-such-session")).andReturn(null);
        EasyMock.replay(sessionDao);

        Assert.assertEquals(null, sessionService.getValidSSOSession("no-such-session"));
        EasyMock.verify(sessionDao);

        EasyMock.reset(sessionDao);

        EasyMock.expect(sessionDao.getValidSSOSession("existing-session"))
                .andReturn(new SSOSession(1));
        EasyMock.replay(sessionDao);

        SSOSession session = sessionService.getValidSSOSession("existing-session");
        Assert.assertNotNull(session);
        Assert.assertEquals(1, session.getId());
        EasyMock.verify(sessionDao);
    }
}
