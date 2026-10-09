package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * The built-in admin must not be usable with the default password from the documentation: it is created with a
 * temporary password, a re-run of the install script does not restore the default one, and R1.7.9 marks an existing
 * admin that still has it. Plain JDBC against a database prepared by create_test_database.sh; the URL can be
 * overridden with -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class DefaultAdminPasswordMigrationTest {

    private static final Path   MI_DIR           = Paths.get("..", "superfly-sql", "mi");
    private static final String DEFAULT_HASH     = "0d7d1771e08bc48f6fe90b14a89c505d344a0f6f1a54de3b10a93466cb235f96";
    private static final String DEFAULT_SALT     = "3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f";
    private static final String PUBLISHED_HOTP   = "f81ead99b99b7f0a91a441621ab1d1248860848f";

    private Connection conn;
    private String     savedPassword;
    private String     savedSalt;
    private String     savedTemp;
    private String     savedHotpSalt;

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        try (PreparedStatement ps = conn.prepareStatement(
                "select user_password, salt, is_password_temp, hotp_salt from users where user_name = 'admin'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue("admin is created by the install scripts", rs.next());
            savedPassword = rs.getString(1);
            savedSalt = rs.getString(2);
            savedTemp = rs.getString(3);
            savedHotpSalt = rs.getString(4);
        }
    }

    @After
    public void tearDown() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "update users set user_password = ?, salt = ?, is_password_temp = ?, hotp_salt = ? where user_name = 'admin'")) {
            ps.setString(1, savedPassword);
            ps.setString(2, savedSalt);
            ps.setString(3, savedTemp);
            ps.setString(4, savedHotpSalt);
            ps.executeUpdate();
        } finally {
            conn.close();
        }
    }

    @Test
    public void cleanInstallLeavesAdminWithTemporaryPassword() {
        assertEquals("Y", savedTemp);
    }

    @Test
    public void installScriptRerunKeepsTheChangedPassword() throws Exception {
        setAdmin("changed-hash", "changed-salt", "N", "custom-hotp");

        run("R1.2.0", "R1.2.0_SSO_DML.sql");

        assertEquals("changed-hash", admin("user_password"));
        assertEquals("changed-salt", admin("salt"));
        assertEquals("N", admin("is_password_temp"));
    }

    @Test
    public void hotpSaltOfInstallIsNotThePublishedOne() throws Exception {
        setAdmin(DEFAULT_HASH, DEFAULT_SALT, "Y", PUBLISHED_HOTP);

        run("R1.2.2", "R1.2.2_SSO_DML.sql");

        assertNotEquals(PUBLISHED_HOTP, admin("hotp_salt"));
        String generated = admin("hotp_salt");
        run("R1.2.2", "R1.2.2_SSO_DML.sql");
        assertEquals("a generated salt is kept on a re-run", generated, admin("hotp_salt"));
    }

    @Test
    public void migrationMarksAdminWithDefaultPassword() throws Exception {
        setAdmin(DEFAULT_HASH, DEFAULT_SALT, "N", PUBLISHED_HOTP);

        run("R1.7.9", "R1.7.9_SSO.sql");
        run("R1.7.9", "R1.7.9_SSO.sql");

        assertEquals("Y", admin("is_password_temp"));
        assertEquals(DEFAULT_HASH, admin("user_password"));
        assertNotEquals(PUBLISHED_HOTP, admin("hotp_salt"));
    }

    @Test
    public void migrationDoesNotTouchAdminWithChangedPassword() throws Exception {
        setAdmin("pbkdf2-sha256$600000$changed", DEFAULT_SALT, "N", "custom-hotp");

        run("R1.7.9", "R1.7.9_SSO.sql");

        assertEquals("N", admin("is_password_temp"));
        assertEquals("pbkdf2-sha256$600000$changed", admin("user_password"));
        assertEquals("custom-hotp", admin("hotp_salt"));
    }

    private void setAdmin(String password, String salt, String temp, String hotpSalt) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "update users set user_password = ?, salt = ?, is_password_temp = ?, hotp_salt = ? where user_name = 'admin'")) {
            ps.setString(1, password);
            ps.setString(2, salt);
            ps.setString(3, temp);
            ps.setString(4, hotpSalt);
            assertEquals(1, ps.executeUpdate());
        }
    }

    private String admin(String column) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select " + column + " from users where user_name = 'admin'");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }

    private void run(String version, String file) throws Exception {
        String script = new String(Files.readAllBytes(MI_DIR.resolve(version).resolve(file)), StandardCharsets.UTF_8)
                .replaceAll("(?s)/\\*.*?\\*/", "")
                .replaceAll("(?m)^\\s*--.*$", "");
        for (String statement : script.split(";")) {
            String sql = statement.trim();
            if (!sql.isEmpty()) {
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
        }
    }
}
