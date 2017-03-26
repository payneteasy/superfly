package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmtpServerDaoTest extends AbstractDaoTest {
    private SmtpServerDao smtpServerDao;
    private SubsystemDao subsystemDao;

    private static boolean createdServer = false;

    @Autowired
    public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
        this.smtpServerDao = smtpServerDao;
    }

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Before
    public void setUp() {
        if (!createdServer) {
            UISmtpServer server = new UISmtpServer();
            server.setName("the name");
            server.setHost("host");
            server.setPort(2525);
            server.setUsername("username");
            server.setPassword("password");
            smtpServerDao.createSmtpServer(server);
            createdServer = true;
        }
    }

    @Test
    public void testUpdateSmtpServer() {
        UISmtpServer server = getAnySmtpServer();
        server.setName("testName");
        server.setHost("host.com");
        server.setPort(null);
        server.setUsername("user");
        server.setPassword("pwd");
        RoutineResult result = smtpServerDao.updateSmtpServer(server);
        assertRoutineResult(result);
    }

    private UISmtpServer getAnySmtpServer() {
        long serverId = getAnySmtpServerId();
        return smtpServerDao.getSmtpServer(serverId);
    }

    private long getAnySmtpServerId(){
        List<UISmtpServerForList> servers = smtpServerDao.listSmtpServers();
        UISmtpServerForList server = servers.get(0);
        return server.getId();
    }

    @Test
    public void testGetSmtpServers() {
        List<UISmtpServerForList> list = smtpServerDao.listSmtpServers();
        assertNotNull("Servers list should not be null", list);
        assertTrue("Servers list should not be empty", list.size() > 0);
    }

    @Test
    public void testCreateSmtpServer() {
        UISmtpServer server = createServer("the server name");
        server.setSsl(true);
        RoutineResult result = smtpServerDao.createSmtpServer(server);
        assertRoutineResult(result);
        assertNotNull("ID must be generated", server.getId());
        server = smtpServerDao.getSmtpServer(server.getId());
        assertTrue(server.isSsl());
    }

    private UISmtpServer createServer(String name) {
        UISmtpServer server = new UISmtpServer();
        server.setName(name);
        server.setHost("host");
        server.setPort(2525);
        server.setUsername("username");
        server.setPassword("password");
        return server;
    }

    @Test
    public void testGetSmtpServersForFilter() {
        List<UISmtpServerForFilter> list = smtpServerDao.getSmtpServersForFilter();
        assertNotNull("Servers list should not be null", list);
        assertTrue("Servers list should not be empty", list.size() > 0);
    }

    @Test
    public void testGetSmtpServer() {
        assertNotNull("Server not found", smtpServerDao.getSmtpServer(getAnySmtpServerId()));
    }

    @Test
    public void testGetSmtpServerBySubsystemIdentifier() {
        smtpServerDao.getSmtpServerBySubsystemIdentifier("subsystem");
    }

    @Test
    public void testDeleteSmtpServer() {
        UISmtpServer server = createServer("for-successful-deletion");
        smtpServerDao.createSmtpServer(server);
        long forSuccess = server.getId();

        server = createServer("for-failed-deletion");
        smtpServerDao.createSmtpServer(server);
        long forFailure = server.getId();
        UISmtpServerForFilter serverForFilter = new UISmtpServerForFilter();
        serverForFilter.setId(server.getId());
        serverForFilter.setName(server.getName());

        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("for smtp server deletion");
        subsystem.setCallbackUrl("some info");
        subsystem.setSmtpServer(serverForFilter);
        subsystem.setTitle("The Subsystem");
        subsystem.setSubsystemUrl("url");
        subsystem.setLandingUrl("url");
        subsystemDao.createSubsystem(subsystem);

        RoutineResult result = smtpServerDao.deleteSmtpServer(forSuccess);
        assertRoutineResult(result);

        result = smtpServerDao.deleteSmtpServer(forFailure);
        Assert.assertFalse("OK".equals(result.getStatus()));
    }
}
