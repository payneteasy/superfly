package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The pending OTP master key (R1.7.8) and its procedures: the pending key never touches master_key until it is
 * confirmed, and the confirmation is a compare-and-set that loses to a concurrent reset. Plain JDBC against a
 * database prepared by create_test_database.sh; the URL can be overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class OtpPendingMasterKeyProcedureTest {

    private static final String USER = "otp-pending-user";
    private static final Path MIGRATION = Paths.get("..", "superfly-sql", "mi", "R1.7.8", "R1.7.8_SSO.sql");

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection(
                System.getProperty("sso.db.url", "jdbc:mysql://localhost/ssotest"), "sso", "123sso123");
        cleanup();
        long roleId = queryLong("select min(role_id) from roles");
        try (CallableStatement cs = conn.prepareCall("{call ui_create_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, "hash");
            cs.setString(3, "otp-pending@example.com");
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
        }
        call("save_google_auth_master_key", "v2:active");
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
    public void migrationCanBeAppliedAgain() throws Exception {
        String script = new String(Files.readAllBytes(MIGRATION), StandardCharsets.UTF_8).replaceAll("(?m)^--.*$", "");
        for (int run = 0; run < 2; run++) {
            for (String statement : script.split(";")) {
                String sql = statement.trim();
                if (!sql.isEmpty()) {
                    try (Statement st = conn.createStatement()) {
                        st.execute(sql);
                    }
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement("select column_type, is_nullable from information_schema.columns"
                + " where table_schema = database() and table_name = 'users' and column_name = 'otp_pending_master_key'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("varchar(128)", rs.getString(1));
                assertEquals("YES", rs.getString(2));
            }
        }
    }

    @Test
    public void pendingKeyIsStoredApartFromTheActiveOne() throws Exception {
        call("save_otp_pending_master_key", "v2:pending-1");
        call("save_otp_pending_master_key", "v2:pending-2");

        assertEquals("v2:pending-2", pendingKey());
        assertEquals("v2:active", column("master_key"));
    }

    @Test
    public void confirmationMovesThePendingKeyToTheActiveOne() throws Exception {
        call("save_otp_pending_master_key", "v2:pending");

        assertEquals(1, confirm("v2:pending"));

        assertEquals("v2:pending", column("master_key"));
        assertNull(pendingKey());
    }

    @Test
    public void confirmationOfAReplacedPendingKeyChangesNothing() throws Exception {
        call("save_otp_pending_master_key", "v2:read-by-confirmation");
        // a concurrent reset between reading the pending key and confirming it
        call("save_otp_pending_master_key", "v2:concurrent");

        assertEquals(0, confirm("v2:read-by-confirmation"));

        assertEquals("v2:active", column("master_key"));
        assertEquals("v2:concurrent", pendingKey());
    }

    @Test
    public void confirmationComparesTheKeyCaseSensitively() throws Exception {
        call("save_otp_pending_master_key", "v2:Pending");

        assertEquals(0, confirm("v2:pending"));

        assertEquals("v2:active", column("master_key"));
        assertEquals("v2:Pending", pendingKey());
    }

    @Test
    public void confirmationWithoutPendingKeyChangesNothing() throws Exception {
        assertEquals(0, confirm("v2:active"));
        assertEquals(0, confirm(null));

        assertEquals("v2:active", column("master_key"));
        assertNull(pendingKey());
    }

    private void call(String procedure, String key) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call " + procedure + "(?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, key);
            cs.execute();
        }
    }

    private int confirm(String pendingKey) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call confirm_otp_pending_master_key(?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, pendingKey);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt("updated_count");
            }
        }
    }

    private String pendingKey() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call get_otp_pending_master_key_by_user_name(?)}")) {
            cs.setString(1, USER);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString("totp_key");
            }
        }
    }

    private String column(String name) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select " + name + " from users where user_name = ?")) {
            ps.setString(1, USER);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    private long queryLong(String sql) throws Exception {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getLong(1);
        }
    }

    private void cleanup() throws Exception {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("delete from user_roles where user_user_id in (select user_id from users where user_name = '" + USER + "')");
            st.executeUpdate("delete from user_history where user_user_id in (select user_id from users where user_name = '" + USER + "')");
            st.executeUpdate("delete from users where user_name = '" + USER + "'");
        }
    }
}
