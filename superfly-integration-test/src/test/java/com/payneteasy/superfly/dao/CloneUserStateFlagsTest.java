package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A cloned user starts in the state of a user created by ui_create_user, whatever the state of the template user is,
 * and the R2.0.0 migration fills the lock flag of the users cloned before. Plain JDBC against a database prepared by
 * create_test_database.sh; the URL can be overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class CloneUserStateFlagsTest {

    private static final String TEMPLATE_USER = "cloneflags-template";
    private static final String CLONE_USER = "cloneflags-clone";
    private static final String NEW_USER = "cloneflags-new";
    private static final String MIGRATION = "../superfly-sql/mi/R2.0.0/R2.0.0_SSO.sql";
    private static final String MIGRATION_STEP_START = "update users set is_account_locked";

    private static final List<String> STATE_COLUMNS = Arrays.asList(
            "is_account_locked", "is_account_suspended", "logins_failed", "hotp_logins_failed", "hotp_counter",
            "last_login_date", "completed", "is_otp_optional", "otp_otp_type_id", "master_key",
            "otp_last_used_step", "otp_pending_master_key");

    /** Every state column of the template differs from the value a new user gets. */
    private static final String TEMPLATE_STATE_UPDATE = "update users set is_account_locked = 'Y',"
            + " is_account_suspended = 'Y', logins_failed = 3, hotp_logins_failed = 2, hotp_counter = 7,"
            + " last_login_date = now(), completed = 'N', is_otp_optional = 'Y',"
            + " otp_otp_type_id = (select otp_type_id from otp_types where otp_code = 'google_auth'),"
            + " master_key = 'template-master-key', otp_last_used_step = 12345,"
            + " otp_pending_master_key = 'template-pending-key' where user_id = ";

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        cleanup();
    }

    @After
    public void tearDown() throws Exception {
        try {
            cleanup();
        } finally {
            conn.close();
        }
    }

    @Test
    public void clonedUserIsNotLockedWhenTheTemplateIsLocked() throws Exception {
        long templateId = createUser(TEMPLATE_USER);
        update("update users set is_account_locked = 'Y' where user_id = " + templateId);

        cloneUser(templateId);

        assertEquals("N", state(CLONE_USER).get("is_account_locked"));
    }

    @Test
    public void clonedUserHasTheStateOfAUserCreatedByUiCreateUser() throws Exception {
        long templateId = createUser(TEMPLATE_USER);
        createUser(NEW_USER);
        update(TEMPLATE_STATE_UPDATE + templateId);
        assertNotEquals("the template must not be in the state of a new user", state(NEW_USER), state(TEMPLATE_USER));

        cloneUser(templateId);

        Map<String, String> clone = state(CLONE_USER);
        assertEquals(state(NEW_USER), clone);
        assertEquals("N", clone.get("is_account_locked"));
        assertEquals("N", clone.get("is_account_suspended"));
        assertNull(clone.get("logins_failed"));
        assertEquals("0", clone.get("hotp_logins_failed"));
        assertEquals("0", clone.get("hotp_counter"));
        assertNull(clone.get("last_login_date"));
        assertEquals("Y", clone.get("completed"));
        assertEquals("N", clone.get("is_otp_optional"));
        assertNull(clone.get("otp_otp_type_id"));
        assertNull(clone.get("master_key"));
        assertNull(clone.get("otp_last_used_step"));
        assertNull(clone.get("otp_pending_master_key"));
    }

    @Test
    public void migrationFillsTheEmptyLockFlagOnly() throws Exception {
        // a user cloned before the fix has no lock flag
        long emptyId = createUser(CLONE_USER);
        long lockedId = createUser(TEMPLATE_USER);
        long unlockedId = createUser(NEW_USER);
        update("update users set is_account_locked = null where user_id = " + emptyId);
        update("update users set is_account_locked = 'Y' where user_id = " + lockedId);

        runMigrationStep();
        runMigrationStep();

        assertEquals("N", lockFlag(emptyId));
        assertEquals("Y", lockFlag(lockedId));
        assertEquals("N", lockFlag(unlockedId));
    }

    private void runMigrationStep() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(MIGRATION)), StandardCharsets.UTF_8)
                .replaceAll("(?m)^\\s*--.*$", "");
        int start = sql.indexOf(MIGRATION_STEP_START);
        assertTrue("lock flag step not found in the migration", start >= 0);
        int end = sql.indexOf(';', start);
        assertTrue("lock flag step is not terminated", end > start);
        update(sql.substring(start, end));
    }

    private long createUser(String userName) throws Exception {
        long roleId = queryLong("select min(role_id) from roles");
        try (CallableStatement cs = conn.prepareCall("{call ui_create_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, userName);
            cs.setString(2, "hash");
            cs.setString(3, userName + "@example.com");
            cs.setLong(4, roleId);
            cs.setString(5, "n");
            cs.setString(6, "s");
            cs.setString(7, "q");
            cs.setString(8, "a");
            cs.setString(9, "salt");
            cs.setString(10, "hotp-salt");
            cs.setString(11, "N");
            cs.setString(12, null);
            cs.setString(13, "org");
            cs.setString(14, null);
            cs.setString(15, "N");
            cs.registerOutParameter(16, Types.INTEGER);
            cs.execute();
            long id = cs.getLong(16);
            assertTrue(id > 0);
            return id;
        }
    }

    private long cloneUser(long templateId) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call ui_clone_user(?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, CLONE_USER);
            cs.setString(2, "hash");
            cs.setString(3, "salt");
            cs.setString(4, "hotp-salt");
            cs.setString(5, "clone@example.com");
            cs.setLong(6, templateId);
            cs.setString(7, "N");
            cs.setString(8, null);
            cs.registerOutParameter(9, Types.INTEGER);
            cs.execute();
            long id = cs.getLong(9);
            assertTrue(id > 0);
            return id;
        }
    }

    private Map<String, String> state(String userName) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "select " + String.join(", ", STATE_COLUMNS) + " from users where user_name = ?")) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("user " + userName + " must exist", rs.next());
                Map<String, String> state = new LinkedHashMap<>();
                for (int i = 0; i < STATE_COLUMNS.size(); i++) {
                    state.put(STATE_COLUMNS.get(i), rs.getString(i + 1));
                }
                return state;
            }
        }
    }

    private String lockFlag(long userId) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select is_account_locked from users where user_id = " + userId)) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    private long queryLong(String sql) throws Exception {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private void update(String sql) throws Exception {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private void cleanup() throws Exception {
        String names = "'" + TEMPLATE_USER + "', '" + CLONE_USER + "', '" + NEW_USER + "'";
        String ids = "select user_id from users where user_name in (" + names + ")";
        for (String table : new String[]{"user_roles", "user_role_actions", "user_complects", "user_preferences", "user_history"}) {
            update("delete from " + table + " where user_user_id in (" + ids + ")");
        }
        update("delete from users where user_name in (" + names + ")");
    }
}
