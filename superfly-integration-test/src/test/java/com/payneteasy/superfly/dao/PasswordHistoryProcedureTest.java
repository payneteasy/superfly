package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * get_user_password_history_and_current_password must list the user's own passwords newest first and must not
 * let temporary (admin reset) passwords take a place in the history window. Plain JDBC against a database
 * prepared by create_test_database.sh; the URL can be overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class PasswordHistoryProcedureTest {

    private static final String USER = "pwdhist-user";
    private static final int HISTORY_DEPTH = 4;
    // stands for the real hash: the stored value is exactly what this encoder produces
    private static final PasswordEncoder ENCODER = (plain, salt) -> "enc(" + plain + ")";

    private Connection conn;
    private long userId;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection(
                System.getProperty("sso.db.url", "jdbc:mysql://localhost/ssotest"), "sso", "123sso123");
        cleanup();
        long roleId = queryLong("select min(role_id) from roles");

        // created by an admin with a temporary password, then the user replaces it, again and again
        try (CallableStatement cs = conn.prepareCall("{call ui_create_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, stored("T0"));
            cs.setString(3, "pwdhist@example.com");
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
        changeTemp("P1");
        for (String next : new String[]{"P2", "P3", "P4", "P5"}) {
            reset("T" + next);
            changeTemp(next);
        }
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
    public void lastPasswordsOfTheUserAreCheckedNewestFirst() throws Exception {
        List<PasswordSaltPair> history = history();
        PasswordCheckContext ctx = new PasswordCheckContext("x", ENCODER, history);

        assertEquals(stored("P5"), history.get(0).getPassword());
        assertEquals(stored("P4"), history.get(1).getPassword());
        assertEquals(stored("P3"), history.get(2).getPassword());
        assertEquals(stored("P2"), history.get(3).getPassword());
        for (String own : new String[]{"P2", "P3", "P4", "P5"}) {
            assertTrue(own + " must be rejected", ctx.isPasswordExist(own, HISTORY_DEPTH));
        }
    }

    @Test
    public void pendingTemporaryPasswordDoesNotTakeAPlaceInTheHistory() throws Exception {
        reset("T6");
        List<PasswordSaltPair> history = history();
        PasswordCheckContext ctx = new PasswordCheckContext("x", ENCODER, history);

        assertEquals(stored("P5"), history.get(0).getPassword());
        for (String own : new String[]{"P2", "P3", "P4", "P5"}) {
            assertTrue(own + " must be rejected", ctx.isPasswordExist(own, HISTORY_DEPTH));
        }
        assertFalse("temporary password is not the user's own one", ctx.isPasswordExist("T6", HISTORY_DEPTH));
    }

    private static String stored(String plain) {
        return ENCODER.encode(plain, "salt");
    }

    private void changeTemp(String plain) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call change_temp_password(?,?)}")) {
            cs.setString(1, USER);
            cs.setString(2, stored(plain));
            cs.execute();
        }
    }

    private void reset(String plain) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call reset_password(?,?)}")) {
            cs.setLong(1, userId);
            cs.setString(2, stored(plain));
            cs.execute();
        }
    }

    private List<PasswordSaltPair> history() throws Exception {
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
