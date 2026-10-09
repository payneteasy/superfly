package com.payneteasy.superfly.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * R1.8.0 converts the plain subsystem tokens to sha256:hex, keeps NULL as is and does not hash twice. Plain JDBC
 * against a database prepared by create_test_database.sh; the URL can be overridden with
 * -Dsso.db.url=jdbc:mysql://host:port/db.
 */
public class SubsystemTokenMigrationTest {

    private static final Path MI_DIR = Paths.get("..", "superfly-sql", "mi");
    private static final String[] NAMES = {"h7a-plain", "h7a-null", "h7a-empty", "h7a-proc"};

    private Connection conn;

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        cleanUp();
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
    public void plainTokenIsHashedAndRerunDoesNotChangeIt() throws Exception {
        insert("h7a-plain", "plain-token-value");

        migrate();
        String hashed = token("h7a-plain");
        assertEquals("sha256:" + sha256("plain-token-value"), hashed);

        migrate();
        assertEquals(hashed, token("h7a-plain"));
    }

    @Test
    public void nullAndEmptyTokensStayAsTheyAre() throws Exception {
        insert("h7a-null", null);
        insert("h7a-empty", "");

        migrate();

        assertNull(token("h7a-null"));
        assertEquals("", token("h7a-empty"));
    }

    @Test
    public void createProcedureKeepsTheWholeHash() throws Exception {
        String hash = "sha256:" + sha256("any");
        try (Statement st = conn.createStatement()) {
            st.execute("call ui_create_subsystem('h7a-proc', 'title', 'cb', 'Y', 'N', null, '" + hash
                    + "', 'http://u', 'http://l', null, null, null, null, @id)");
        }
        assertEquals(hash, token("h7a-proc"));
    }

    private void insert(String name, String token) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into subsystems (subsystem_name, subsystem_title, subsystem_url, landing_url, subsystem_token) "
                        + "values (?, 't', 'http://u', 'http://l', ?)")) {
            ps.setString(1, name);
            ps.setString(2, token);
            assertEquals(1, ps.executeUpdate());
        }
    }

    private String token(String name) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "select subsystem_token from subsystems where subsystem_name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    private void cleanUp() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("delete from subsystems where subsystem_name = ?")) {
            for (String name : NAMES) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        }
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private void migrate() throws Exception {
        String script = new String(Files.readAllBytes(MI_DIR.resolve("R1.8.0").resolve("R1.8.0_SSO.sql")), StandardCharsets.UTF_8)
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
