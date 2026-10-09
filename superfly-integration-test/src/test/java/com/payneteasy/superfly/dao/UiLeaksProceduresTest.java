package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * User list columns, subsystems without an SMTP server, user cloning and the private key lookup of an unknown
 * subsystem. Plain JDBC against a database prepared by create_test_database.sh.
 */
public class UiLeaksProceduresTest {

    private static final String SUBSYSTEM = "uileaks-ssys";
    private static final String TEMPLATE_USER = "uileaks-template";
    private static final String CLONE_USER = "uileaks-clone";
    private static final List<String> LIST_COLUMNS = Arrays.asList(
            "user_id", "user_name", "is_account_locked", "logins_failed", "last_login_date",
            "email", "is_account_suspended", "hotp_counter");

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
    public void usersListHasNoPasswordColumnAndSortsByEachListedColumn() throws Exception {
        createTemplateUser();
        // sort field numbers used by ListUsersPage: id, name, locked, logins failed, last login date
        for (int order = 1; order <= 5; order++) {
            try (CallableStatement cs = conn.prepareCall("{call ui_get_users_list(?,?,?,?,?,?,?,?)}")) {
                cs.setInt(1, 0);
                cs.setInt(2, 100);
                cs.setInt(3, order);
                cs.setString(4, "asc");
                cs.setString(5, null);
                cs.setNull(6, Types.INTEGER);
                cs.setNull(7, Types.INTEGER);
                cs.setNull(8, Types.INTEGER);
                try (ResultSet rs = cs.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        columns.add(md.getColumnLabel(i));
                    }
                    assertEquals(LIST_COLUMNS, columns);
                    assertFalse(columns.contains("user_password"));

                    Comparable<Object> previous = null;
                    while (rs.next()) {
                        @SuppressWarnings("unchecked")
                        Comparable<Object> current = (Comparable<Object>) rs.getObject(order);
                        if (previous != null && current != null) {
                            assertTrue("rows must be ordered by " + columns.get(order - 1),
                                    previous.compareTo(current) <= 0);
                        }
                        if (current != null) {
                            previous = current;
                        }
                    }
                }
            }
        }
    }

    @Test
    public void subsystemWithoutSmtpServerKeepsNullReference() throws Exception {
        long id = createSubsystem(0);
        assertNull(smtpServerId());

        try (CallableStatement cs = conn.prepareCall("{call ui_edit_subsystem_properties(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setLong(1, id);
            cs.setString(2, SUBSYSTEM);
            cs.setString(3, "title");
            cs.setString(4, "cb");
            cs.setString(5, "N");
            cs.setString(6, "N");
            cs.setLong(7, 0);
            cs.setString(8, "token");
            cs.setString(9, "http://localhost/");
            cs.setString(10, "http://localhost/x");
            cs.setString(11, "http://localhost/y");
            cs.setString(12, null);
            cs.setString(13, null);
            cs.setString(14, "RSA");
            cs.execute();
        }
        assertNull(smtpServerId());
    }

    @Test
    public void clonedUserGetsEmptyPersonalDataAndSecretAnswer() throws Exception {
        long templateId = createTemplateUser();
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
            assertTrue(cs.getLong(9) > 0);
        }
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select name, surname, secret_question, secret_answer from users where user_name = '" + CLONE_USER + "'")) {
            assertTrue(rs.next());
            assertEquals("", rs.getString("name"));
            assertEquals("", rs.getString("surname"));
            assertEquals("", rs.getString("secret_question"));
            assertEquals("", rs.getString("secret_answer"));
        }
    }

    @Test
    public void privateKeyOfUnknownSubsystemIsNull() throws Exception {
        assertNull(privateKey("uileaks-no-such-subsystem"));
    }

    @Test
    public void privateKeyOfSubsystemWithoutKeyIsNull() throws Exception {
        createSubsystem(0);
        assertNull(privateKey(SUBSYSTEM));
    }

    private String privateKey(String subsystem) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call get_subsystem_private_key(?)}")) {
            cs.setString(1, subsystem);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue("exactly one row expected", rs.next());
                String key = rs.getString(1);
                assertFalse(rs.next());
                return key;
            }
        }
    }

    private Long smtpServerId() throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select ssrv_ssrv_id from subsystems where subsystem_name = '" + SUBSYSTEM + "'")) {
            assertTrue(rs.next());
            long value = rs.getLong(1);
            return rs.wasNull() ? null : value;
        }
    }

    private long createSubsystem(long smtpServerId) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call ui_create_subsystem(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, SUBSYSTEM);
            cs.setString(2, "title");
            cs.setString(3, "cb");
            cs.setString(4, "N");
            cs.setString(5, "N");
            cs.setLong(6, smtpServerId);
            cs.setString(7, "token");
            cs.setString(8, "http://localhost/");
            cs.setString(9, "http://localhost/x");
            cs.setString(10, "http://localhost/x");
            cs.setString(11, null);
            cs.setString(12, null);
            cs.setString(13, "RSA");
            cs.registerOutParameter(14, Types.INTEGER);
            cs.execute();
            long id = cs.getLong(14);
            return id;
        }
    }

    private long createTemplateUser() throws Exception {
        long roleId;
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("select min(role_id) from roles")) {
            assertTrue(rs.next());
            roleId = rs.getLong(1);
        }
        try (CallableStatement cs = conn.prepareCall("{call ui_create_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, TEMPLATE_USER);
            cs.setString(2, "hash");
            cs.setString(3, "template@example.com");
            cs.setLong(4, roleId);
            cs.setString(5, "tn");
            cs.setString(6, "ts");
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
            return cs.getLong(16);
        }
    }

    private void cleanup() throws Exception {
        try (Statement st = conn.createStatement()) {
            String ids = "select user_id from users where user_name in ('" + TEMPLATE_USER + "', '" + CLONE_USER + "')";
            for (String table : new String[]{"user_roles", "user_role_actions", "user_complects", "user_preferences", "user_history"}) {
                st.executeUpdate("delete from " + table + " where user_user_id in (" + ids + ")");
            }
            st.executeUpdate("delete from users where user_name in ('" + TEMPLATE_USER + "', '" + CLONE_USER + "')");
            st.executeUpdate("delete from subsystems where subsystem_name = '" + SUBSYSTEM + "'");
        }
    }
}
