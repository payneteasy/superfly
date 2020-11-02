package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.SessionDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.session.UISession;
import com.payneteasy.superfly.notification.LogoutNotification;
import com.payneteasy.superfly.notification.Notifier;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.utils.RandomGUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Transactional
public class SessionServiceImpl implements SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    private SessionDao sessionDao;
    private Notifier notifier;
    private LoggerSink loggerSink;

    @Required
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Required
    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Required
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    public List<UISession> getExpiredSessions() {
        return sessionDao.getExpiredSessions();
    }

    public List<UISession> getInvalidSessions() {
        return sessionDao.getInvalidSessions();
    }

    public RoutineResult expireInvalidSessions() {
        return sessionDao.expireInvalidSessions();
    }

    public List<UISession> deleteExpiredSessionsAndNotify() {
        return deleteExpiredSessionsAndNotify(null);
    }

    public List<UISession> deleteExpiredSessionsAndNotify(Date beforeWhat) {
        List<UISession> sessions = sessionDao.deleteExpiredSessions(beforeWhat);
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted " + sessions.size() + " sessions"
                    + (sessions.size() > 0 ? ", going to notify subsystems" : ""));
        }
        if (!sessions.isEmpty()) {
            Map<String, List<String>> callbackUriToSessionIds = new HashMap<>();
            for (UISession session : sessions) {
                if (session.getCallbackInformation() != null && session.isSendCallbacks()) {
                    String uri = session.getCallbackInformation();
                    List<String> sessionIds = callbackUriToSessionIds.get(uri);
                    if (sessionIds == null) {
                        sessionIds = new ArrayList<>();
                        callbackUriToSessionIds.put(uri, sessionIds);
                    }
                    sessionIds.add(String.valueOf(session.getId()));
                }
            }
            List<LogoutNotification> notifications = new ArrayList<>(callbackUriToSessionIds.size());
            for (Entry<String, List<String>> entry : callbackUriToSessionIds.entrySet()) {
                LogoutNotification notification = new LogoutNotification();
                notification.setCallbackUri(entry.getKey());
                notification.setSessionIds(entry.getValue());
                notifications.add(notification);
            }
            if (!notifications.isEmpty()) {
                notifier.notifyAboutLogout(notifications);
            }
        }
        return sessions;
    }

    public List<UISession> deleteExpiredAndOldSessionsAndNotify(int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -seconds);
        return deleteExpiredSessionsAndNotify(calendar.getTime());
    }

    @Override
    public SSOSession getValidSSOSession(String ssoSessionIdentifier) {
        return sessionDao.getValidSSOSession(ssoSessionIdentifier);
    }

    @Override
    public SSOSession createSSOSession(String username) {
        SSOSession session = sessionDao.createSSOSession(username,
                generateUniqueSSOSessionToken());
        loggerSink.info(logger, "SSO_SESSION_CREATED", true, username);
        return session;
    }

    private String generateUniqueSSOSessionToken() {
        return "SSO-" + new RandomGUID().toString().replaceAll("-", "");
    }

    @Override
    public void deleteExpiredSSOSessions(int maxAgeSeconds) {
        sessionDao.deleteExpiredSSOSessions(maxAgeSeconds);
    }

    @Override
    public void deleteExpiredTokens(int maxSubsystemTokenAgeSeconds) {
        sessionDao.deleteExpiredTokens(maxSubsystemTokenAgeSeconds);
    }

    @Override
    public void deleteSSOSession(String ssoSessionIdentifier) {
        sessionDao.deleteSSOSession(ssoSessionIdentifier);
    }

    @Override
    public void touchSessions(String sessionIds) {
        sessionDao.touchSessions(sessionIds);
    }

}
