package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * register_user must mark a user without OTP as OTP-optional, otherwise the client rejects the login with
 * "No OTP type for user" (the column default is 'N' since R1.7.7). Plain JDBC against a database prepared by
 * create_test_database.sh; the URL can be overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class RegisterUserOtpOptionalProcedureTest {

    private static final String USER_NONE = "regotp-none";
    private static final String USER_GOOGLE = "regotp-google";

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
    public void userWithoutOtpIsOtpOptional() throws Exception {
        register(USER_NONE, "none");

        assertEquals("Y", otpOptional(USER_NONE));
    }

    @Test
    public void userWithGoogleAuthIsOtpRequired() throws Exception {
        register(USER_GOOGLE, "google_auth");

        assertEquals("N", otpOptional(USER_GOOGLE));
    }

    private void register(String userName, String otpCode) throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call register_user(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, userName);
            cs.setString(2, "enc(pwd)");
            cs.setString(3, userName + "@example.com");
            cs.setString(4, "superfly");
            cs.setString(5, null);
            cs.setString(6, "n");
            cs.setString(7, "s");
            cs.setString(8, "q");
            cs.setString(9, "a");
            cs.setString(10, "salt");
            cs.setString(11, "N");
            cs.setString(12, "hotp-salt");
            cs.setString(13, null);
            cs.setString(14, "org");
            cs.setString(15, otpCode);
            cs.registerOutParameter(16, Types.INTEGER);
            cs.execute();
            assertTrue(cs.getLong(16) > 0);
        }
    }

    private String otpOptional(String userName) throws Exception {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("select is_otp_optional from users where user_name = '" + userName + "'")) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    private void cleanup() throws Exception {
        try (Statement st = conn.createStatement()) {
            String ids = "(select user_id from users where user_name in ('" + USER_NONE + "', '" + USER_GOOGLE + "'))";
            st.executeUpdate("delete from user_roles where user_user_id in " + ids);
            st.executeUpdate("delete from user_history where user_user_id in " + ids);
            st.executeUpdate("delete from users where user_name in ('" + USER_NONE + "', '" + USER_GOOGLE + "')");
        }
    }
}
