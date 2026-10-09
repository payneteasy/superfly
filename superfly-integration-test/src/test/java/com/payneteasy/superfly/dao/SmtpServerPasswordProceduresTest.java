package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.SmtpServerPassword;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The SMTP password column holds CryptoService ciphertext, the list procedure does not return it, an empty password
 * on edit keeps the stored one, and the startup encryption procedures touch only plain passwords. The Spring context
 * creates every DAO, so it also checks that the entities match the procedures.
 */
public class SmtpServerPasswordProceduresTest extends AbstractDaoTest {

    private static final String NAME  = "smtp-pw-test";
    private static final String PLAIN = "plain-test-password";

    private SmtpServerDao smtpServerDao;
    private Connection    conn;
    private long          id;

    @Autowired
    public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
        this.smtpServerDao = smtpServerDao;
    }

    @Before
    public void setUp() throws Exception {
        conn = TestDatabase.connect();
        cleanUp();
        UISmtpServer server = server(PLAIN);
        assertRoutineResult(smtpServerDao.createSmtpServer(server));
        id = server.getId();
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
    public void listDoesNotReturnPassword() throws Exception {
        try (CallableStatement cs = conn.prepareCall("{call ui_get_smtp_servers_list()}");
             ResultSet rs = cs.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                assertFalse("password".equalsIgnoreCase(meta.getColumnLabel(i)));
            }
        }
        List<UISmtpServerForList> list = smtpServerDao.listSmtpServers();
        assertTrue(list.stream().anyMatch(s -> s.getId() == id && NAME.equals(s.getName())));
        List<UISmtpServerForFilter> filter = smtpServerDao.getSmtpServersForFilter();
        assertTrue(filter.stream().anyMatch(s -> s.getId() == id));
    }

    @Test
    public void serverIsReadWithPassword() {
        assertEquals(PLAIN, smtpServerDao.getSmtpServer(id).getPassword());
    }

    @Test
    public void editWithEmptyPasswordKeepsStoredPassword() throws Exception {
        UISmtpServer server = server(null);
        server.setId(id);
        server.setHost("smtp2.example.com");
        assertRoutineResult(smtpServerDao.updateSmtpServer(server));
        assertEquals(PLAIN, storedPassword());
        assertEquals("smtp2.example.com", smtpServerDao.getSmtpServer(id).getHost());

        server.setPassword("");
        assertRoutineResult(smtpServerDao.updateSmtpServer(server));
        assertEquals(PLAIN, storedPassword());

        server.setPassword("v2:new");
        assertRoutineResult(smtpServerDao.updateSmtpServer(server));
        assertEquals("v2:new", storedPassword());
    }

    @Test
    public void passwordColumnFitsCiphertext() throws Exception {
        String ciphertext = "v2:" + "A".repeat(252);
        UISmtpServer server = server(ciphertext);
        server.setId(id);
        assertRoutineResult(smtpServerDao.updateSmtpServer(server));

        assertEquals(ciphertext, storedPassword());
    }

    @Test
    public void plainPasswordsAreListedAndEncryptedOnesAreNot() throws Exception {
        assertTrue(plainPasswordServers().contains(id));

        smtpServerDao.encryptSmtpServerPassword(id, "v2:ciphertext");

        assertEquals("v2:ciphertext", storedPassword());
        assertFalse(plainPasswordServers().contains(id));
    }

    @Test
    public void encryptNeverOverwritesAnEncryptedPassword() throws Exception {
        smtpServerDao.encryptSmtpServerPassword(id, "v2:first");
        smtpServerDao.encryptSmtpServerPassword(id, "v2:second");

        assertEquals("v2:first", storedPassword());
    }

    private List<Long> plainPasswordServers() {
        List<SmtpServerPassword> servers = smtpServerDao.getSmtpServersWithPlainPassword();
        for (SmtpServerPassword server : servers) {
            assertNotNull(server.getName());
            assertFalse(server.getPassword().startsWith("v2:"));
        }
        return servers.stream().map(SmtpServerPassword::getId).toList();
    }

    private String storedPassword() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("select password from smtp_servers where ssrv_id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    private static UISmtpServer server(String password) {
        UISmtpServer server = new UISmtpServer();
        server.setName(NAME);
        server.setHost("smtp.example.com");
        server.setPort(2525);
        server.setUsername("mailer");
        server.setPassword(password);
        server.setFrom("noreply@example.com");
        return server;
    }

    private void cleanUp() throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("delete from smtp_servers where server_name = ?")) {
            ps.setString(1, NAME);
            ps.executeUpdate();
        }
    }
}
