package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A subsystem session is opened only for a user who is neither locked nor suspended, whichever way it is opened
 * (authenticate, pseudoAuthenticate, exchangeSubsystemToken). The flags are read with a lock on the user row right
 * before the session row is inserted, so a lock committed after the earlier checks of the call is seen, and a lock
 * being applied at that moment is waited for.
 */
public class SessionIssueGuardDaoTest extends AbstractDaoTest {
    // the database may be reused between runs (-Dsso.db.skipCreate=true), so the names are unique per run
    private static final String RUN      = Long.toString(System.currentTimeMillis(), 36);
    private static final String ACTION   = "session-guard-action";
    // the DAO stores the password as given: it is the hash the service layer would pass
    private static final String PASSWORD = "abc";
    // how long a call that must be held back by a row lock is given to return
    private static final long   HELD_BACK_MILLIS = 1000;
    private static final String LATE_LOCK_MESSAGE =
            "no session must be opened for a user locked after the first checks of the call";

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
        created.setName("session-guard-" + RUN);
        created.setCallbackUrl("no-callback");
        created.setTitle(created.getName());
        created.setSubsystemUrl(created.getName() + "-url");
        created.setLandingUrl(created.getName() + "-url");
        assertRoutineResult(subsystemDao.createSubsystem(created));

        UIRole role = new UIRole();
        role.setRoleName("session-guard-role");
        role.setPrincipalName("session-guard-role");
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

    // ---- a user who may log in gets a session as before

    @Test
    public void pseudoAuthenticateOpensSessionForActiveUser() {
        TestUser user = createUser("active", false);

        assertSessionOpened(user, userDao.pseudoAuthenticate(user.name, subsystem.getName()), ACTION);
    }

    @Test
    public void pseudoAuthenticateOpensSessionForUserWithoutLockFlag() {
        TestUser user = createUser("no-flag", false);
        jdbc.update("update users set is_account_locked = null where user_id = ?", user.id);

        assertSessionOpened(user, userDao.pseudoAuthenticate(user.name, subsystem.getName()), ACTION);
    }

    @Test
    public void authenticateOpensSessionForActiveUser() {
        TestUser user = createUser("auth-active", false);

        assertSessionOpened(user, authenticate(user), ACTION);
    }

    @Test
    public void authenticateOpensTemporaryPasswordSessionForActiveUser() {
        TestUser user = createUser("auth-temp", true);

        assertSessionOpened(user, authenticate(user), "action_temp_password");
    }

    @Test
    public void exchangeOpensSessionForActiveUser() {
        TestUser user = createUser("token-active", false);

        assertSessionOpened(user, userDao.exchangeSubsystemToken(issueToken(user), subsystem.getName()), ACTION);
    }

    // ---- a locked or suspended user gets no session

    @Test
    public void pseudoAuthenticateOpensNoSessionForLockedUser() {
        TestUser user = createUser("locked", false);
        assertRoutineResult(userDao.lockUser(user.id));

        Assert.assertNull(userDao.pseudoAuthenticate(user.name, subsystem.getName()));
        assertNoSessions(user);
    }

    @Test
    public void pseudoAuthenticateOpensNoSessionForSuspendedUser() {
        TestUser user = createUser("suspended", false);
        assertRoutineResult(userDao.suspendUser(user.id));

        Assert.assertNull(userDao.pseudoAuthenticate(user.name, subsystem.getName()));
        assertNoSessions(user);
    }

    @Test
    public void pseudoAuthenticateOpensNoSessionWhenOnlyTheSuspendFlagIsSet() {
        TestUser user = createUser("susp-flag", false);
        jdbc.update("update users set is_account_suspended = 'Y' where user_id = ?", user.id);

        Assert.assertNull(userDao.pseudoAuthenticate(user.name, subsystem.getName()));
        assertNoSessions(user);
    }

    @Test
    public void lockedUserCannotAuthenticate() {
        TestUser user = createUser("auth-locked", false);
        assertRoutineResult(userDao.lockUser(user.id));

        Assert.assertNull(authenticate(user));
        assertNoSessions(user);
    }

    @Test
    public void lockedUserCannotExchangeToken() {
        TestUser user = createUser("token-locked", false);
        String token = issueToken(user);
        // the flag only: the token stays, so the refusal is the one of the exchange itself, which keeps the token
        jdbc.update("update users set is_account_locked = 'Y' where user_id = ?", user.id);

        Assert.assertNull(userDao.exchangeSubsystemToken(token, subsystem.getName()));
        Assert.assertEquals(1, count("select count(*) from subsystem_tokens where token = ?", token));
        assertNoSessions(user);
    }

    // ---- a lock committed after the checks made at the start of the call (the transaction still sees the old state)

    @Test
    public void pseudoAuthenticateOpensNoSessionWhenTheUserIsLockedAfterTheSnapshot() throws Exception {
        TestUser user = createUser("late-pseudo", false);

        Assert.assertFalse(LATE_LOCK_MESSAGE, opensSessionAfterLateLock(user,
                "{call get_user_actions(?, ?)}", user.name, subsystem.getName()));
        assertNoSessions(user);
    }

    @Test
    public void authenticateOpensNoSessionWhenTheUserIsLockedAfterTheSnapshot() throws Exception {
        TestUser user = createUser("late-auth", false);

        Assert.assertFalse(LATE_LOCK_MESSAGE, opensSessionAfterLateLock(user,
                "{call authenticate(?, ?, ?, ?, ?, ?)}", user.name, PASSWORD, null, subsystem.getName(), null, null));
        assertNoSessions(user);
    }

    @Test
    public void authenticateOpensNoTemporaryPasswordSessionWhenTheUserIsLockedAfterTheSnapshot() throws Exception {
        TestUser user = createUser("late-temp", true);

        Assert.assertFalse(LATE_LOCK_MESSAGE, opensSessionAfterLateLock(user,
                "{call authenticate(?, ?, ?, ?, ?, ?)}", user.name, PASSWORD, null, subsystem.getName(), null, null));
        assertNoSessions(user);
    }

    @Test
    public void exchangeOpensNoSessionWhenTheUserIsLockedAfterTheSnapshot() throws Exception {
        TestUser user = createUser("late-token", false);
        String token = issueToken(user);

        Assert.assertFalse(LATE_LOCK_MESSAGE, opensSessionAfterLateLock(user,
                "{call exchange_subsystem_token(?, ?)}", token, subsystem.getName()));
        assertNoSessions(user);
    }

    // ---- a lock that is being applied at the moment

    @Test
    public void sessionWaitsForTheLockBeingAppliedAndIsNotOpened() throws Exception {
        TestUser user = createUser("wait-lock", false);

        Assert.assertFalse("no session must be opened for a user whose lock was committed meanwhile",
                openedAfterHeldBackByLockUpdate(user, true));
        assertNoSessions(user);
    }

    @Test
    public void sessionWaitsForTheUpdateOfTheUserAndIsOpenedWhenItIsRolledBack() throws Exception {
        TestUser user = createUser("wait-undo", false);

        Assert.assertTrue("a session must be opened when the lock was rolled back",
                openedAfterHeldBackByLockUpdate(user, false));
        Assert.assertEquals(1, count("select count(*) from sessions where user_user_id = ? and session_expired = 'N'",
                user.id));
    }

    // ---- helpers

    private TestUser createUser(String tag, boolean temporaryPassword) {
        UIUserForCreate user = new UIUserForCreate();
        user.setUsername("sg-" + tag + "-" + RUN);
        user.setPassword(PASSWORD);
        user.setEmail(user.getUsername() + "@example.com");
        user.setName("n");
        user.setSurname("s");
        user.setSecretQuestion("");
        user.setSecretAnswer("");
        user.setHotpSalt("DEADBEEF");
        user.setIsPasswordTemp(temporaryPassword);
        assertRoutineResult(userDao.createUser(user));
        assertRoutineResult(userDao.addSubsystemWithRole(user.getId(), roleId));
        assertRoutineResult(userDao.changeUserRoleActions(user.getId(), String.valueOf(roleActionId), ""));
        Assert.assertEquals(temporaryPassword ? "Y" : "N",
                jdbc.queryForObject("select is_password_temp from users where user_id = ?", String.class, user.getId()));
        return new TestUser(user.getId(), user.getUsername());
    }

    private AuthSession authenticate(TestUser user) {
        return userDao.authenticate(user.name, PASSWORD, null, subsystem.getName(), null, null);
    }

    private String issueToken(TestUser user) {
        SSOSession ssoSession = sessionDao.createSSOSession(user.name, "SSO-" + user.name);
        String token = "TOK-" + user.name;
        Assert.assertNotNull(subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), token));
        return token;
    }

    private void assertSessionOpened(TestUser user, AuthSession session, String expectedAction) {
        Assert.assertNotNull("a session must be opened", session);
        Assert.assertNotNull(session.getSessionId());
        Assert.assertEquals(user.name, session.getUsername());
        Assert.assertTrue("the actions of the user must be returned: " + expectedAction,
                session.getRoles().stream().flatMap(role -> role.getActions().stream())
                        .anyMatch(action -> expectedAction.equals(action.getActionName())));
        Assert.assertEquals(1, count("select count(*) from sessions where user_user_id = ? and sess_id = ?"
                + " and session_expired = 'N'", user.id, session.getSessionId()));
    }

    private void assertNoSessions(TestUser user) {
        Assert.assertEquals("no session row must be inserted", 0,
                count("select count(*) from sessions where user_user_id = ?", user.id));
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    /**
     * Calls the procedure in a transaction that read the user before the lock was committed by another connection,
     * the way a login does when the user is locked a moment after its first checks. The transaction still sees the
     * user as not locked, so only a read of the committed state can tell. The transaction is committed at the end,
     * as the service layer commits it.
     */
    private boolean opensSessionAfterLateLock(TestUser user, String call, String... args) throws SQLException {
        try (Connection connection = TestDatabase.connect()) {
            connection.setAutoCommit(false);
            Assert.assertEquals("N", lockFlag(connection, user));
            jdbc.update("update users set is_account_locked = 'Y' where user_id = ?", user.id);
            Assert.assertEquals("the snapshot of the transaction must predate the lock", "N", lockFlag(connection, user));

            boolean opened = returnsRow(connection, call, args);
            connection.commit();
            return opened;
        }
    }

    /**
     * Locks the user in a transaction that is not finished yet, calls pseudoAuthenticate meanwhile and checks that
     * the call is held back by the update of the user row; then the update is committed or rolled back and whether
     * the call opened a session is returned.
     */
    private boolean openedAfterHeldBackByLockUpdate(TestUser user, boolean commit) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (Connection updater = TestDatabase.connect(); Connection caller = TestDatabase.connect()) {
            updater.setAutoCommit(false);
            try (Statement statement = updater.createStatement()) {
                statement.executeUpdate("update users set is_account_locked = 'Y' where user_id = " + user.id);
            }

            Future<Boolean> opened = executor.submit(
                    () -> returnsRow(caller, "{call get_user_actions(?, ?)}", user.name, subsystem.getName()));
            try {
                opened.get(HELD_BACK_MILLIS, TimeUnit.MILLISECONDS);
                Assert.fail("the call must wait until the update of the user is finished");
            } catch (TimeoutException held) {
                // the call is held back by the lock on the user row
            }

            if (commit) {
                updater.commit();
            } else {
                updater.rollback();
            }
            return opened.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    private static String lockFlag(Connection connection, TestUser user) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "select coalesce(is_account_locked, 'N') from users where user_id = " + user.id)) {
            Assert.assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    /** Whether the first result set of the call has a row: a session comes back as the rows of its actions. */
    private static boolean returnsRow(Connection connection, String call, String... args) throws SQLException {
        try (CallableStatement statement = connection.prepareCall(call)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            if (!statement.execute()) {
                return false;
            }
            try (ResultSet rs = statement.getResultSet()) {
                return rs.next();
            }
        }
    }

    private static final class TestUser {
        final long   id;
        final String name;

        TestUser(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
