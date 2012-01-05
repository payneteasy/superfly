package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import junit.framework.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
	
	public void testGetSmtpServers() {
		List<UISmtpServerForList> list = smtpServerDao.listSmtpServers();
		assertNotNull("Servers list should not be null", list);
		assertTrue("Servers list should not be empty", list.size() > 0);
	}

	public void testCreateSmtpServer() {
        UISmtpServer server = createServer("the server name");
        RoutineResult result = smtpServerDao.createSmtpServer(server);
        assertRoutineResult(result);
		assertNotNull("ID must be generated", server.getId());
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

    public void testGetSmtpServersForFilter() {
		List<UISmtpServerForFilter> list = smtpServerDao.getSmtpServersForFilter();
		assertNotNull("Servers list should not be null", list);
		assertTrue("Servers list should not be empty", list.size() > 0);
	}

	public void testGetSmtpServer() {
        Assert.assertNotNull("Server not found", smtpServerDao.getSmtpServer(getAnySmtpServerId()));
	}

	public void testGetSmtpServerBySubsystemIdentifier() {
        smtpServerDao.getSmtpServerBySubsystemIdentifier("subsystem");
	}

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
        subsystem.setCallbackInformation("some info");
        subsystem.setSmtpServer(serverForFilter);
        subsystemDao.createSubsystem(subsystem);

        RoutineResult result = smtpServerDao.deleteSmtpServer(forSuccess);
        assertRoutineResult(result);

        result = smtpServerDao.deleteSmtpServer(forFailure);
        assertFalse("OK".equals(result.getStatus()));
    }
}
