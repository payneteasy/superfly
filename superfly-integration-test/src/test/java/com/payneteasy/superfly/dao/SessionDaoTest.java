package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.session.UISession;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class SessionDaoTest extends AbstractDaoTest {
    private SessionDao sessionDao;

    @Autowired
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Test
    public void testGetInvalidSessions() {
        List<UISession> sessions = sessionDao.getInvalidSessions();
        assertNotNull("Must get some result", sessions);
    }

    @Test
    public void testGetExpiredSessions() {
        List<UISession> sessions = sessionDao.getExpiredSessions();
        assertNotNull("Must get some result", sessions);
    }

    @Test
    public void testExpireInvalidSessions() {
        RoutineResult result = sessionDao.expireInvalidSessions();
        assertRoutineResult(result);
    }

    @Test
    public void testDeleteExpiredSessions() {
        List<UISession> sessions = sessionDao.deleteExpiredSessions(new Date());
        assertNotNull("Must get some result", sessions);
    }

    @Test
    public void testDeleteExpiredSSOSession() {
        sessionDao.deleteExpiredSSOSessions(1);
    }

    @Test
    public void testDeleteExpiredTokens() {
        sessionDao.deleteExpiredTokens(1);
    }
}
