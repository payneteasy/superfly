package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.session.UISession;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Locking, suspending (also by the failed logins limit) and deleting a user end all sessions of that user the way
 * an SSO logout does: the SSO sessions and their subsystem tokens are deleted, and every subsystem session of the
 * user, including one opened without SSO, is marked expired, so the scheduler sends the logout notifications.
 * Unlocking restores nothing.
 */
public class AccountLockSessionsDaoTest extends AbstractDaoTest {
    // the database may be reused between runs (-Dsso.db.skipCreate=true), so the names are unique per run
    private static final String RUN    = Long.toString(System.currentTimeMillis(), 36);
    private static final String ACTION = "lock-sessions-action";

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
        created.setName("lock-sessions-" + RUN);
        created.setCallbackUrl("no-callback");
        created.setTitle(created.getName());
        created.setSubsystemUrl(created.getName() + "-url");
        created.setLandingUrl(created.getName() + "-url");
        assertRoutineResult(subsystemDao.createSubsystem(created));

        UIRole role = new UIRole();
        role.setRoleName("lock-sessions-role");
        role.setPrincipalName("lock-sessions-role");
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
    public void lockEndsAllSessionsOfTheUserOnly() {
        UserSessions user = userWithSessions("lock");
        UserSessions other = userWithSessions("lock-other");

        assertRoutineResult(userDao.lockUser(user.userId));

        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void suspendEndsAllSessionsOfTheUserOnly() {
        UserSessions user = userWithSessions("susp");
        UserSessions other = userWithSessions("susp-other");

        assertRoutineResult(userDao.suspendUser(user.userId));

        Assert.assertEquals("Y", userFlag(user, "is_account_suspended"));
        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_SUSPEND", user));
    }

    @Test
    public void failedLoginsLimitEndsAllSessionsOfTheUserOnly() {
        UserSessions user = userWithSessions("auto");
        UserSessions other = userWithSessions("auto-other");
        jdbc.update("update users set logins_failed = 3 where user_id = ?", user.userId);

        userDao.lockoutConditionnally(user.userName, 3, "PASSWORD");

        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        assertSessionsAlive(other);
        Assert.assertEquals(1, subsystemEvents("ACCOUNT_LOCK", user));
    }

    @Test
    public void failedLoginsLimitLocksAUserWithoutTheLockFlag() {
        UserSessions user = userWithSessions("auto-null");
        jdbc.update("update users set is_account_locked = null, logins_failed = 3 where user_id = ?", user.userId);

        userDao.lockoutConditionnally(user.userName, 3, "PASSWORD");

        Assert.assertEquals("Y", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
    }

    @Test
    public void deleteEndsAllSessionsOfTheUser() {
        UserSessions user = userWithSessions("del");

        assertRoutineResult(userDao.deleteUser(user.userId));

        assertSessionsEnded(user);
    }

    @Test
    public void unlockDoesNotRestoreTheSessions() {
        UserSessions user = userWithSessions("unlock");

        assertRoutineResult(userDao.lockUser(user.userId));
        assertRoutineResult(userDao.unlockUser(user.userId));

        Assert.assertEquals("N", userFlag(user, "is_account_locked"));
        assertSessionsEnded(user);
        for (String token : user.unusedTokens) {
            Assert.assertNull(userDao.exchangeSubsystemToken(token, subsystem.getName()));
        }
    }

    /**
     * Two SSO sessions, each with a subsystem session obtained by a token and one more token not used yet,
     * plus a subsystem session opened without SSO.
     */
    private UserSessions userWithSessions(String tag) {
        UIUserForCreate user = new UIUserForCreate();
        user.setUsername("ls-" + tag + "-" + RUN);
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

        UserSessions result = new UserSessions(user.getId(), user.getUsername());
        for (int i = 1; i <= 2; i++) {
            SSOSession ssoSession = sessionDao.createSSOSession(result.userName, "SSO-" + result.userName + "-" + i);
            result.ssoSessions.add(ssoSession);

            String exchanged = issueToken(ssoSession, "used-" + i);
            AuthSession session = userDao.exchangeSubsystemToken(exchanged, subsystem.getName());
            Assert.assertNotNull(session);
            result.sessionIds.add(session.getSessionId());

            result.unusedTokens.add(issueToken(ssoSession, "unused-" + i));
        }
        AuthSession withoutSso = userDao.pseudoAuthenticate(result.userName, subsystem.getName());
        Assert.assertNotNull(withoutSso);
        result.sessionIds.add(withoutSso.getSessionId());

        Assert.assertEquals(2, count("select count(*) from sessions where user_user_id = ? and ssos_ssos_id is not null",
                result.userId));
        Assert.assertEquals(1, count("select count(*) from sessions where user_user_id = ? and ssos_ssos_id is null",
                result.userId));
        assertSessionsAlive(result);
        return result;
    }

    private String issueToken(SSOSession ssoSession, String suffix) {
        String token = "TOK-" + ssoSession.getIdentifier() + "-" + suffix;
        Assert.assertNotNull(subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), token));
        return token;
    }

    private void assertSessionsEnded(UserSessions user) {
        Assert.assertEquals(0, count("select count(*) from sso_sessions where user_user_id = ?", user.userId));
        for (SSOSession ssoSession : user.ssoSessions) {
            Assert.assertNull(sessionDao.getValidSSOSession(ssoSession.getIdentifier()));
            Assert.assertEquals(0, count("select count(*) from subsystem_tokens where ssos_ssos_id = ?",
                    ssoSession.getId()));
        }
        Assert.assertEquals(0, count("select count(*) from sessions where user_user_id = ? and session_expired <> 'Y'",
                user.userId));
        // the expired sessions stay until the scheduler deletes them and notifies the subsystems
        Set<Long> expired = sessionDao.getExpiredSessions().stream().map(UISession::getId).collect(Collectors.toSet());
        Assert.assertTrue(expired.containsAll(user.sessionIds));
    }

    private void assertSessionsAlive(UserSessions user) {
        Assert.assertEquals(2, count("select count(*) from sso_sessions where user_user_id = ?", user.userId));
        for (SSOSession ssoSession : user.ssoSessions) {
            Assert.assertNotNull(sessionDao.getValidSSOSession(ssoSession.getIdentifier()));
        }
        for (String token : user.unusedTokens) {
            Assert.assertEquals(1, count("select count(*) from subsystem_tokens where token = ?", token));
        }
        Assert.assertEquals(3, count("select count(*) from sessions where user_user_id = ? and session_expired = 'N'",
                user.userId));
    }

    /** Events written for the test subsystem, where the user has a role. */
    private int subsystemEvents(String eventCode, UserSessions user) {
        return count("select count(*) from events e join event_types t on t.event_type_id = e.event_type_id"
                + " where t.event_code = ? and e.event_data = ? and e.subsystem_id = ?",
                eventCode, user.userName, subsystem.getId());
    }

    private String userFlag(UserSessions user, String column) {
        return jdbc.queryForObject("select " + column + " from users where user_id = ?", String.class, user.userId);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    private static final class UserSessions {
        final long              userId;
        final String            userName;
        final List<SSOSession>  ssoSessions  = new ArrayList<>();
        final List<String>      unusedTokens = new ArrayList<>();
        final List<Long>        sessionIds   = new ArrayList<>();

        UserSessions(long userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }
}
