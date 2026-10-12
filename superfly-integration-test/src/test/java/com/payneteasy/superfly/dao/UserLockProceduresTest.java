package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the procedures that lock a user answer to their caller. login_locked reports the lock it has just made in
 * error_message, which is what the service audits as an automatic lock; ui_lock_user and ui_delete_user answer
 * OK. All three lock the user and end the sessions of that user the same way.
 */
public class UserLockProceduresTest extends AbstractDaoTest {
    // the database may be reused between runs (-Dsso.db.skipCreate=true), so the names are unique per run
    private static final String RUN    = Long.toString(System.currentTimeMillis(), 36);
    private static final String ACTION = "user-lock-action";

    private static final int    MAX_LOGINS_FAILED = 3;
    private static final String LOCKED_MARKER     = "ACCOUNT_LOCKED";

    private static UISubsystem subsystem;
    private static long        roleId;
    private static long        roleActionId;

    private SessionDao   sessionDao;
    private SubsystemDao subsystemDao;
    private UserDao      userDao;
    private RoleDao      roleDao;
    private ActionDao    actionDao;
    private JdbcTemplate jdbc;

    @Autowired
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Autowired
    public void setActionDao(ActionDao actionDao) {
        this.actionDao = actionDao;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Before
    public void setUp() {
        if (subsystem != null) {
            return;
        }
        UISubsystem created = new UISubsystem();
        created.setName("user-lock-" + RUN);
        created.setCallbackUrl("no-callback");
        created.setTitle(created.getName());
        created.setSubsystemUrl(created.getName() + "-url");
        created.setLandingUrl(created.getName() + "-url");
        assertRoutineResult(subsystemDao.createSubsystem(created));

        UIRole role = new UIRole();
        role.setRoleName("user-lock-role");
        role.setPrincipalName("user-lock-role");
        role.setSubsystemId(created.getId());
        assertRoutineResult(roleDao.createRole(role));
        roleId = role.getRoleId();

        actionDao.saveActions(created.getName(), Collections.singletonList(new ActionToSave(ACTION)));
        long actionId = jdbc.queryForObject("select a.actn_id from actions a"
                + " join subsystems s on s.ssys_id = a.ssys_ssys_id"
                + " where s.subsystem_name = ? and a.action_name = ?", Long.class, created.getName(), ACTION);
        assertRoutineResult(roleDao.changeRoleActions(roleId, String.valueOf(actionId), ""));
        roleActionId = jdbc.queryForObject("select ract_id from role_actions where role_role_id = ? and actn_actn_id = ?",
                Long.class, roleId, actionId);
        subsystem = created;
    }

    @Test
    public void failedPasswordsLimitIsReportedToTheCaller() {
        UserWithSessions user = userWithSessions("pwd");
        UserWithSessions other = userWithSessions("pwd-other");
        failPasswordLogins(user, MAX_LOGINS_FAILED);

        RoutineResult result = userDao.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");

        assertLockReported(result);
        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void failedOtpLimitIsReportedToTheCaller() {
        UserWithSessions user = userWithSessions("otp");
        for (int i = 0; i < MAX_LOGINS_FAILED; i++) {
            userDao.incrementHOTPLoginsFailed(user.userName);
        }

        RoutineResult result = userDao.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "HOTP");

        assertLockReported(result);
        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void failedLoginsBelowTheLimitAreNotReported() {
        UserWithSessions user = userWithSessions("below");
        failPasswordLogins(user, MAX_LOGINS_FAILED - 1);

        RoutineResult result = userDao.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");

        assertOkWithoutMessage(result);
        Assert.assertEquals("N", userFlag(user, "is_account_locked"));
        assertSessionsAlive(user);
        Assert.assertEquals(0, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void lockedUserIsNotReportedAgain() {
        UserWithSessions user = userWithSessions("again");
        failPasswordLogins(user, MAX_LOGINS_FAILED);
        assertLockReported(userDao.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD"));

        RoutineResult second = userDao.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");

        assertOkWithoutMessage(second);
        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void unknownUserIsNotReported() {
        assertOkWithoutMessage(userDao.lockoutConditionnally("no-such-user-" + RUN, MAX_LOGINS_FAILED, "PASSWORD"));
    }

    @Test
    public void serviceAuditsTheAutomaticLockOnce() {
        UserWithSessions user = userWithSessions("audit");
        RecordingLoggerSink sink = new RecordingLoggerSink();
        UserServiceImpl service = new UserServiceImpl();
        service.setUserDao(userDao);
        service.setLoggerSink(sink);
        failPasswordLogins(user, MAX_LOGINS_FAILED - 1);

        service.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");
        Assert.assertTrue(sink.records.toString(), sink.records.isEmpty());

        failPasswordLogins(user, 1);
        service.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");
        Assert.assertEquals(Collections.singletonList("AUTO_LOCK_USER " + user.userName
                + " reason=PASSWORD, maxLoginsFailed=" + MAX_LOGINS_FAILED), sink.records);

        service.lockoutConditionnally(user.userName, MAX_LOGINS_FAILED, "PASSWORD");
        Assert.assertEquals(1, sink.records.size());
    }

    @Test
    public void lockUserAnswersOk() {
        UserWithSessions user = userWithSessions("lock");
        UserWithSessions other = userWithSessions("lock-other");

        RoutineResult result = userDao.lockUser(user.userId);

        assertOkWithoutMessage(result);
        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void deleteUserLocksAndEndsSessions() {
        UserWithSessions user = userWithSessions("del");
        UserWithSessions other = userWithSessions("del-other");

        RoutineResult result = userDao.deleteUser(user.userId);

        assertOkWithoutMessage(result);
        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    private void assertLockReported(RoutineResult result) {
        Assert.assertNotNull("Routine result cannot be null", result);
        Assert.assertTrue("Routine result must be OK", result.isOk());
        Assert.assertEquals(LOCKED_MARKER, result.getErrorMessage());
    }

    private void assertOkWithoutMessage(RoutineResult result) {
        Assert.assertNotNull("Routine result cannot be null", result);
        Assert.assertTrue("Routine result must be OK", result.isOk());
        Assert.assertNull(result.getErrorMessage());
    }

    private void failPasswordLogins(UserWithSessions user, int times) {
        for (int i = 0; i < times; i++) {
            Assert.assertEquals("N", userDao.getUserLoginStatus(user.userName, "wrong-" + i, null,
                    subsystem.getName(), "127.0.0.1"));
        }
    }

    /**
     * An SSO session with a subsystem session obtained by a token and one more token not used yet,
     * plus a subsystem session opened without SSO.
     */
    private UserWithSessions userWithSessions(String tag) {
        UIUserForCreate user = new UIUserForCreate();
        user.setUsername("ulp-" + tag + "-" + RUN);
        user.setPassword("abc");
        user.setEmail(user.getUsername() + "@example.com");
        user.setName("n");
        user.setSurname("s");
        user.setSecretQuestion("");
        user.setSecretAnswer("");
        user.setHotpSalt("DEADBEEF");
        assertRoutineResult(userDao.createUser(user));
        assertRoutineResult(userDao.addSubsystemWithRole(user.getId(), roleId));
        assertRoutineResult(userDao.changeUserRoleActions(user.getId(), String.valueOf(roleActionId), ""));

        UserWithSessions result = new UserWithSessions(user.getId(), user.getUsername());
        result.ssoSession = sessionDao.createSSOSession(result.userName, "SSO-" + result.userName);

        AuthSession session = userDao.exchangeSubsystemToken(issueToken(result.ssoSession, "used"), subsystem.getName());
        Assert.assertNotNull(session);
        result.sessionIds.add(session.getSessionId());
        result.unusedToken = issueToken(result.ssoSession, "unused");

        AuthSession withoutSso = userDao.pseudoAuthenticate(result.userName, subsystem.getName());
        Assert.assertNotNull(withoutSso);
        result.sessionIds.add(withoutSso.getSessionId());

        assertSessionsAlive(result);
        return result;
    }

    private String issueToken(SSOSession ssoSession, String suffix) {
        String token = "TOK-" + ssoSession.getIdentifier() + "-" + suffix;
        Assert.assertNotNull(subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), token));
        return token;
    }

    private void assertSessionsEnded(UserWithSessions user) {
        Assert.assertEquals(0, count("select count(*) from sso_sessions where user_user_id = ?", user.userId));
        Assert.assertNull(sessionDao.getValidSSOSession(user.ssoSession.getIdentifier()));
        Assert.assertEquals(0, count("select count(*) from subsystem_tokens where ssos_ssos_id = ?",
                user.ssoSession.getId()));
        // the expired sessions stay until the scheduler deletes them and notifies the subsystems
        Assert.assertEquals(user.sessionIds.size(), count("select count(*) from sessions where user_user_id = ?"
                + " and session_expired = 'Y'", user.userId));
        Assert.assertEquals(0, count("select count(*) from sessions where user_user_id = ? and session_expired <> 'Y'",
                user.userId));
    }

    private void assertSessionsAlive(UserWithSessions user) {
        Assert.assertEquals(1, count("select count(*) from sso_sessions where user_user_id = ?", user.userId));
        Assert.assertNotNull(sessionDao.getValidSSOSession(user.ssoSession.getIdentifier()));
        Assert.assertEquals(1, count("select count(*) from subsystem_tokens where token = ?", user.unusedToken));
        Assert.assertEquals(user.sessionIds.size(), count("select count(*) from sessions where user_user_id = ?"
                + " and session_expired = 'N'", user.userId));
    }

    /** Events written for the test subsystem, where the user has a role. */
    private int subsystemEvents(String eventCode, UserWithSessions user) {
        return count("select count(*) from events e join event_types t on t.event_type_id = e.event_type_id"
                + " where t.event_code = ? and e.event_data = ? and e.subsystem_id = ?",
                eventCode, user.userName, subsystem.getId());
    }

    private String userFlag(UserWithSessions user, String column) {
        return jdbc.queryForObject("select " + column + " from users where user_id = ?", String.class, user.userId);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private static final class UserWithSessions {
        final long       userId;
        final String     userName;
        final List<Long> sessionIds = new ArrayList<>();
        SSOSession       ssoSession;
        String           unusedToken;

        UserWithSessions(long userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }

    /** Keeps what the service audits: the event, the resource and the details. */
    private static final class RecordingLoggerSink implements LoggerSink {
        final List<String> records = new ArrayList<>();

        @Override
        public void info(Logger logger, String eventType, boolean success, String resourceIdentity) {
            info(logger, eventType, success, resourceIdentity, null);
        }

        @Override
        public void info(Logger logger, String eventType, boolean success, String resourceIdentity, String details) {
            Assert.assertTrue(success);
            records.add(eventType + " " + resourceIdentity + (details == null ? "" : " " + details));
        }
    }
}
