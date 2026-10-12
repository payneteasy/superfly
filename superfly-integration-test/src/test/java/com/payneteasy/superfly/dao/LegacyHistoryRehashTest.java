package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A successful login with a legacy hash rehashes the matching user_history row together with users, and the
 * migration drops legacy history rows outside the reuse check window. Plain JDBC against the test database.
 */
public class LegacyHistoryRehashTest {

    private static final String USER = "legacy-hist-user";
    private static final String MIGRATION = "../superfly-sql/mi/R2.0.0/R2.0.0_SSO.sql";
    private static final String MIGRATION_STEP_START = "delete uh";
    private static final PasswordEncoder LEGACY = (plain, salt) -> "legacy(" + plain + ")";

    private static final Map<String, String> PBKDF2_HASHES = new HashMap<>();

    private Connection conn;
    private long userId;

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        cleanup();
        long roleId = queryLong("select min(role_id) from roles");
        try (CallableStatement cs = conn.prepareCall("{call ui_create_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, LEGACY.encode("first", "salt"));
            cs.setString(3, "legacyhist@example.com");
            cs.setLong(4, roleId);
            cs.setString(5, "n");
            cs.setString(6, "s");
            cs.setString(7, "q");
            cs.setString(8, "a");
            cs.setString(9, "salt");
            cs.setString(10, "hotp-salt");
            cs.setString(11, "Y");
            cs.setString(12, null);
            cs.setString(13, "org");
            cs.setString(14, null);
            cs.setString(15, "N");
            cs.registerOutParameter(16, Types.INTEGER);
            cs.execute();
            userId = cs.getLong(16);
        }
        update("update users set is_password_temp = 'N', is_account_locked = 'N' where user_id = " + userId);
        update("delete from user_history where user_user_id = " + userId);
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
    public void successfulLoginRehashesUsersAndTheMatchingHistoryRowOnly() throws Exception {
        history(1, LEGACY.encode("a", "salt"));
        history(2, pbkdf2("b"));
        history(3, LEGACY.encode("c", "salt"));
        update("update users set user_password = '" + LEGACY.encode("c", "salt") + "' where user_id = " + userId);

        assertEquals(userId, login(pbkdf2("c"), LEGACY.encode("c", "salt")));

        assertEquals(pbkdf2("c"), queryString("select user_password from users where user_id = " + userId));
        Map<Integer, String> rows = historyRows();
        assertEquals(LEGACY.encode("a", "salt"), rows.get(1));
        assertEquals(pbkdf2("b"), rows.get(2));
        assertEquals(pbkdf2("c"), rows.get(3));
    }

    @Test
    public void failedLoginChangesNeitherUsersNorHistory() throws Exception {
        history(1, LEGACY.encode("a", "salt"));
        history(2, LEGACY.encode("c", "salt"));
        update("update users set user_password = '" + LEGACY.encode("c", "salt") + "' where user_id = " + userId);

        assertEquals(0, login(pbkdf2("wrong"), LEGACY.encode("wrong", "salt")));

        assertEquals(LEGACY.encode("c", "salt"), queryString("select user_password from users where user_id = " + userId));
        Map<Integer, String> rows = historyRows();
        assertEquals(LEGACY.encode("a", "salt"), rows.get(1));
        assertEquals(LEGACY.encode("c", "salt"), rows.get(2));
    }

    @Test
    public void reuseOfThePasswordIsStillRejectedAfterRehash() throws Exception {
        history(1, LEGACY.encode("a", "salt"));
        history(2, LEGACY.encode("c", "salt"));
        update("update users set user_password = '" + LEGACY.encode("c", "salt") + "' where user_id = " + userId);
        login(pbkdf2("c"), LEGACY.encode("c", "salt"));

        PasswordCheckContext ctx = new PasswordCheckContext("x", LEGACY, passwordHistory());

        assertTrue(ctx.isPasswordExist("c", 4));
        assertTrue(ctx.isPasswordExist("a", 4));
        assertFalse(ctx.isPasswordExist("other", 4));
    }

    @Test
    public void migrationDropsLegacyRowsOutsideTheWindowOnly() throws Exception {
        // 8 rows: 1, 3 and 6 are legacy; the window is the 5 newest (4..8)
        for (int i = 1; i <= 8; i++) {
            history(i, i == 1 || i == 3 || i == 6 ? LEGACY.encode("p" + i, "salt") : pbkdf2("p" + i));
        }

        runMigrationStep();

        Map<Integer, String> rows = historyRows();
        assertFalse(rows.containsKey(1));
        assertFalse(rows.containsKey(3));
        assertEquals(LEGACY.encode("p6", "salt"), rows.get(6));
        assertEquals(6, rows.size());
        assertEquals(pbkdf2("p2"), rows.get(2));
        assertEquals(pbkdf2("p8"), rows.get(8));

        runMigrationStep();
        assertEquals(rows, historyRows());
    }

    private void runMigrationStep() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get(MIGRATION)), StandardCharsets.UTF_8);
        int start = sql.lastIndexOf(MIGRATION_STEP_START);
        assertTrue("delete step not found in the migration", start >= 0);
        int end = sql.indexOf(';', start);
        assertTrue("delete step is not terminated", end > start);
        update(sql.substring(start, end));
    }

    private static String pbkdf2(String plain) {
        return PBKDF2_HASHES.computeIfAbsent(plain, p -> new Pbkdf2PasswordEncoder().encode(p, "salt"));
    }

    private long login(String hash, String legacyHash) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select int_check_user_password(?, ?, ?, ?, ?)")) {
            ps.setString(1, USER);
            ps.setString(2, hash);
            ps.setString(3, legacyHash);
            ps.setString(4, "127.0.0.1");
            ps.setString(5, "test");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }

    private void history(int number, String hash) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into user_history (user_user_id, user_password, salt, number_history, start_date, end_date) "
                        + "values (?, ?, 'salt', ?, now(), '2999-12-31')")) {
            ps.setLong(1, userId);
            ps.setString(2, hash);
            ps.setInt(3, number);
            ps.executeUpdate();
        }
    }

    private Map<Integer, String> historyRows() throws Exception {
        Map<Integer, String> rows = new LinkedHashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select number_history, user_password from user_history where user_user_id = "
                     + userId + " order by number_history")) {
            while (rs.next()) {
                rows.put(rs.getInt(1), rs.getString(2));
            }
        }
        return rows;
    }

    private List<PasswordSaltPair> passwordHistory() throws Exception {
        List<PasswordSaltPair> result = new ArrayList<>();
        try (CallableStatement cs = conn.prepareCall("{call get_user_password_history_and_current_password(?)}")) {
            cs.setString(1, USER);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    PasswordSaltPair pair = new PasswordSaltPair();
                    pair.setPassword(rs.getString("user_password"));
                    pair.setSalt(rs.getString("salt"));
                    result.add(pair);
                }
            }
        }
        return result;
    }

    private String queryString(String sql) throws Exception {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next());
            String value = rs.getString(1);
            assertNotNull(value);
            return value;
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
        update("delete from user_roles where user_user_id in (select user_id from users where user_name = '" + USER + "')");
        update("delete from user_history where user_user_id in (select user_id from users where user_name = '" + USER + "')");
        update("delete from unauthorised_access where printed_user_name = '" + USER + "'");
        update("delete from users where user_name = '" + USER + "'");
    }
}
