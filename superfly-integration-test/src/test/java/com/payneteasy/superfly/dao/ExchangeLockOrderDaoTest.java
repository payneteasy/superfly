package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Exchanging a subsystem token and locking or suspending the same user at the same time must end without a
 * deadlock: the exchange locks the user row before the token and the SSO session, the order ui_lock_user and
 * ui_suspend_user use (user, sessions, tokens, SSO sessions). Every call runs in a transaction that stays open until
 * the call returns, as in the service layer, and the exchange is stopped right after it took the token by a lock on
 * its SSO session that the test holds. Lock waits are recognized in information_schema.innodb_trx, which needs
 * the PROCESS privilege (the database user of dev-env.sh has it).
 */
public class ExchangeLockOrderDaoTest extends AbstractDaoTest {
    // the database may be reused between runs (-Dsso.db.skipCreate=true), so the names are unique per run
    private static final String RUN     = Long.toString(System.currentTimeMillis(), 36);
    private static final String ACTION  = "lock-order-action";
    private static final long   TIMEOUT_SECONDS = 10;

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
        created.setName("lock-order-" + RUN);
        created.setCallbackUrl("no-callback");
        created.setTitle(created.getName());
        created.setSubsystemUrl(created.getName() + "-url");
        created.setLandingUrl(created.getName() + "-url");
        assertRoutineResult(subsystemDao.createSubsystem(created));

        UIRole role = new UIRole();
        role.setRoleName("lock-order-role");
        role.setPrincipalName("lock-order-role");
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
    public void lockingTheUserWhileTheTokenIsExchangedDoesNotDeadlock() throws Exception {
        lockWhileTheTokenIsExchanged("lock", "{call ui_lock_user(?)}");
    }

    @Test
    public void suspendingTheUserWhileTheTokenIsExchangedDoesNotDeadlock() throws Exception {
        lockWhileTheTokenIsExchanged("suspend", "{call ui_suspend_user(?)}");
    }

    @Test
    public void exchangeWaitsForTheLockBeingAppliedAndKeepsTheToken() throws Exception {
        TestUser user = createUser("pending");
        Token token = issueToken(user);
        boolean opened;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (Connection locker = openTransaction(); Connection exchanger = openTransaction()) {
            execute(locker, "update users set is_account_locked = 'Y' where user_id = " + user.id);

            long exchangerId = connectionId(exchanger);
            Future<Boolean> exchange = executor.submit(() -> callAndCommit(exchanger,
                    "{call exchange_subsystem_token(?, ?)}", token.value, subsystem.getName()));
            awaitLockWait(exchangerId, exchange);

            locker.commit();
            opened = result(exchange, "the exchange");
        } finally {
            executor.shutdownNow();
        }

        Assert.assertFalse("no session must be opened for a user locked meanwhile", opened);
        Assert.assertEquals("the token of a refused exchange must be kept", 1,
                count("select count(*) from subsystem_tokens where token = ?", token.value));
        Assert.assertEquals(0, count("select count(*) from sessions where user_user_id = ?", user.id));
    }

    /**
     * The exchange is stopped after it deleted the token, on the SSO session locked by the test; the lock call is
     * started then and also has to wait; when the test lets the exchange go on, both calls must finish. Whichever
     * of them ends first, no session of the user may stay alive and the user stays locked.
     */
    private void lockWhileTheTokenIsExchanged(String tag, String lockCall) throws Exception {
        TestUser user = createUser(tag);
        Token token = issueToken(user);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (Connection holder = openTransaction(); Connection exchanger = openTransaction();
             Connection locker = openTransaction()) {
            execute(holder, "select ssos_id from sso_sessions where ssos_id = " + token.ssoSessionId + " for update");

            long exchangerId = connectionId(exchanger);
            Future<Boolean> exchange = executor.submit(() -> callAndCommit(exchanger,
                    "{call exchange_subsystem_token(?, ?)}", token.value, subsystem.getName()));
            awaitLockWait(exchangerId, exchange);

            long lockerId = connectionId(locker);
            Future<Boolean> lock = executor.submit(() -> callAndCommit(locker, lockCall, String.valueOf(user.id)));
            awaitLockWait(lockerId, lock);

            holder.rollback();
            // a deadlock rolls one of the two calls back with an error
            result(exchange, "the exchange");
            result(lock, "the lock of the user");
        } finally {
            executor.shutdownNow();
        }

        Assert.assertEquals("Y", jdbc.queryForObject("select is_account_locked from users where user_id = ?",
                String.class, user.id));
        Assert.assertEquals("no session of the user may stay alive", 0,
                count("select count(*) from sessions where user_user_id = ? and session_expired <> 'Y'", user.id));
    }

    private TestUser createUser(String tag) {
        UIUserForCreate user = new UIUserForCreate();
        user.setUsername("lo-" + tag + "-" + RUN);
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
        return new TestUser(user.getId(), user.getUsername());
    }

    private Token issueToken(TestUser user) {
        SSOSession ssoSession = sessionDao.createSSOSession(user.name, "SSO-" + user.name);
        String token = "TOK-" + user.name;
        Assert.assertNotNull(subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), token));
        return new Token(token, ssoSession.getId());
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    /** Waits until the transaction of the connection waits for a lock; fails if the call ends before that. */
    private void awaitLockWait(long connectionId, Future<?> call) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (System.nanoTime() < deadline) {
            if (call.isDone()) {
                result(call, "the call");
                Assert.fail("the call ended without waiting for a lock");
            }
            if (count("select count(*) from information_schema.innodb_trx"
                    + " where trx_mysql_thread_id = ? and trx_state = 'LOCK WAIT'", connectionId) > 0) {
                return;
            }
            // InnoDB refreshes the data of innodb_trx only when nobody has read it for 0.1 s: a faster poll sees stale rows
            Thread.sleep(150);
        }
        Assert.fail("the call did not start waiting for a lock in " + TIMEOUT_SECONDS + " seconds");
    }

    /** The outcome of the call; a failure of the call (a deadlock, for one) fails the test with its cause. */
    private static <T> T result(Future<T> call, String what) throws Exception {
        try {
            return call.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw new AssertionError(what + " failed: " + e.getCause(), e.getCause());
        }
    }

    private static Connection openTransaction() throws SQLException {
        Connection connection = TestDatabase.connect();
        connection.setAutoCommit(false);
        return connection;
    }

    private static long connectionId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select connection_id()")) {
            Assert.assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    /** Calls the procedure and commits the transaction of the connection: whether the first result set has a row. */
    private static boolean callAndCommit(Connection connection, String call, String... args) throws SQLException {
        boolean hasRow;
        try (CallableStatement statement = connection.prepareCall(call)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            if (statement.execute()) {
                try (ResultSet rs = statement.getResultSet()) {
                    hasRow = rs.next();
                }
            } else {
                hasRow = false;
            }
        }
        connection.commit();
        return hasRow;
    }

    private static final class TestUser {
        final long   id;
        final String name;

        TestUser(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final class Token {
        final String value;
        final long   ssoSessionId;

        Token(String value, long ssoSessionId) {
            this.value = value;
            this.ssoSessionId = ssoSessionId;
        }
    }
}
