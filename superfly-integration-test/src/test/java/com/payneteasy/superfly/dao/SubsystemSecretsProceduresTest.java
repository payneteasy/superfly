package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The subsystem token and private key are served only by the authentication procedures, never by the UI lookups.
 * Plain JDBC against a database prepared by create_test_database.sh; the URL can be overridden with
 * -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class SubsystemSecretsProceduresTest {

    private static final String NAME  = "h7b-test-subsystem";
    private static final String TOKEN = "test-token";
    private static final String KEY   = "plain-test-key";

    private Connection conn;
    private long       id;

    @Before
    public void setUp() throws Exception {
        conn = DriverManager.getConnection(
                System.getProperty("sso.db.url", "jdbc:mysql://localhost/ssotest"), "sso", "123sso123");
        cleanUp();
        try (CallableStatement cs = conn.prepareCall("{call ui_create_subsystem(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setString(1, NAME);
            cs.setString(2, "title");
            cs.setString(3, "http://localhost/callback");
            cs.setString(4, "N");
            cs.setString(5, "N");
            cs.setNull(6, Types.INTEGER);
            cs.setString(7, TOKEN);
            cs.setString(8, "http://localhost/");
            cs.setString(9, "http://localhost/landing");
            cs.setNull(10, Types.VARCHAR);
            cs.setString(11, KEY);
            cs.setString(12, "test-public-key");
            cs.setString(13, "RSA_OAEP");
            cs.registerOutParameter(14, Types.INTEGER);
            cs.execute();
            id = cs.getLong(14);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            cleanUp();
        } finally {
            conn.close();
        }
    }

    @Test
    public void uiLookupsDoNotReturnTokenAndPrivateKey() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call ui_get_subsystem_by_name(?)}")) {
            cs.setString(1, NAME);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                assertNull(rs.getString("subsystem_token"));
                assertNull(rs.getString("private_key"));
                assertEquals("test-public-key", rs.getString("public_key"));
            }
        }
        try (CallableStatement cs = conn.prepareCall("{call ui_get_subsystem(?)}")) {
            cs.setLong(1, id);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                assertNull(rs.getString("subsystem_token"));
                assertNull(rs.getString("private_key"));
                assertEquals("test-public-key", rs.getString("public_key"));
            }
        }
    }

    @Test
    public void authProceduresReturnTokenAndKey() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call get_subsystem_auth(?)}")) {
            cs.setString(1, NAME);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(TOKEN, rs.getString("subsystem_token"));
                assertEquals("RSA_OAEP", rs.getString("encryption_algorithm"));
                assertEquals(id, rs.getLong("ssys_id"));
                assertFalse(rs.next());
            }
        }
        assertEquals(KEY, storedKey());
    }

    @Test
    public void editWithoutNewValuesKeepsTokenAndKey() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call ui_edit_subsystem_properties(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}")) {
            cs.setLong(1, id);
            cs.setString(2, NAME);
            cs.setString(3, "new title");
            cs.setString(4, "http://localhost/callback");
            cs.setString(5, "N");
            cs.setString(6, "N");
            cs.setNull(7, Types.INTEGER);
            cs.setNull(8, Types.VARCHAR);
            cs.setString(9, "http://localhost/");
            cs.setString(10, "http://localhost/landing");
            cs.setNull(11, Types.VARCHAR);
            cs.setNull(12, Types.VARCHAR);
            cs.setString(13, "test-public-key");
            cs.setString(14, "RSA_OAEP");
            cs.execute();
        }

        assertEquals(KEY, storedKey());
        try (CallableStatement cs = conn.prepareCall("{call get_subsystem_auth(?)}")) {
            cs.setString(1, NAME);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(TOKEN, rs.getString("subsystem_token"));
            }
        }
    }

    private String storedKey() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call get_subsystem_private_key(?)}")) {
            cs.setString(1, NAME);
            try (ResultSet rs = cs.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString("private_key");
            }
        }
    }

    private void cleanUp() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("delete from subsystems where subsystem_name = ?")) {
            ps.setString(1, NAME);
            ps.executeUpdate();
        }
    }
}
