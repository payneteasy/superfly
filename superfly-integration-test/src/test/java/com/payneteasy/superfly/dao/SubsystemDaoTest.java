package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.model.SubsystemPrivateKey;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SubsystemDaoTest extends AbstractDaoTest {
    private SubsystemDao subsystemDao;
    private SmtpServerDao smtpServerDao;

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Autowired
    public void setSmtpServerDao(SmtpServerDao smtpServerDao) {
        this.smtpServerDao = smtpServerDao;
    }

    @Test
    public void testUpdateSubsystem(){
        UISubsystem subsystem = getAnySubsystem();
        subsystem.setName("testName");
        subsystem.setCallbackUrl("testCallbackInfo");
        subsystem.setLandingUrl("/landing-url");
        RoutineResult result = subsystemDao.updateSubsystem(subsystem);
        assertRoutineResult(result);
    }

    private UISubsystem getAnySubsystem(){
        long subsystemId=getAnySubsystemId();
        return subsystemDao.getSubsystem(subsystemId);
    }

    private long getAnySubsystemId(){
        List<UISubsystemForList> subsystems = subsystemDao.getSubsystems();
        UISubsystemForList subsystemForList = subsystems.get(0);
        return subsystemForList.getId();
    }

    private String getAnySubsystemName(){
        List<UISubsystemForList> subsystems = subsystemDao.getSubsystems();
        UISubsystemForList subsystemForList = subsystems.get(0);
        return subsystemForList.getName();
    }

    @Test
    public void testGetSubsystems() {
        List<UISubsystemForList> list = subsystemDao.getSubsystems();
        assertNotNull("Subsystems list should not be null", list);
        assertTrue("Subsystems list should not be empty", list.size() > 0);
    }

    @Test
    public void testCreateSubsystem() {
        UISmtpServer server = new UISmtpServer();
        server.setName("for-addition-to-subsystem");
        server.setHost("host");
        smtpServerDao.createSmtpServer(server);

        // no smtp server
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("subsystem-name");
        subsystem.setTitle("The Subsystem");
        subsystem.setCallbackUrl("http://no-such-host.dlm");
        subsystem.setLandingUrl("/landing-url");
        subsystem.setSubsystemUrl("/");
        subsystemDao.createSubsystem(subsystem);
        assertNotNull("ID must be generated", subsystem.getId());

        // with smtp server
        UISmtpServerForFilter serverForFilter = new UISmtpServerForFilter();
        serverForFilter.setId(server.getId());
        serverForFilter.setName(server.getName());

        subsystem = new UISubsystem();
        subsystem.setName("subsystem-name-2");
        subsystem.setTitle("The Subsystem");
        subsystem.setCallbackUrl("http://no-such-host.dlm");
        subsystem.setSmtpServer(serverForFilter);
        subsystem.setSubsystemUrl("/");
        subsystem.setLandingUrl("/landing-url");
        subsystemDao.createSubsystem(subsystem);
        assertNotNull("ID must be generated", subsystem.getId());
    }

    @Test
    public void testGetSubsystemsForFilter() {
        List<UISubsystemForFilter> list = subsystemDao.getSubsystemsForFilter();
        assertNotNull("Subsystems list should not be null", list);
        assertTrue("Subsystems list should not be empty", list.size() > 0);
    }

    @Test
    public void testGetSubsystem() {
        subsystemDao.getSubsystem(getAnySubsystemId());
    }

    @Test
    public void testGetSubsystemByName() {
        subsystemDao.getSubsystemByName(getAnySubsystemName());
    }

    @Test
    public void testGetSubsystemAuth() {
        SubsystemAuth auth = subsystemDao.getSubsystemAuth(getAnySubsystemName());
        assertNotNull(auth);
        assertNotNull(auth.getId());
    }

    @Test
    public void testGetSubsystemPrivateKey() {
        UISubsystem subsystem = createSubsystemWithKey("key-read-subsystem", "plain-test-key");

        assertEquals("plain-test-key", subsystemDao.getSubsystemPrivateKey(subsystem.getName()));
        assertNull(subsystemDao.getSubsystemPrivateKey("no-such-subsystem"));
    }

    @Test
    public void testEncryptSubsystemPrivateKey() {
        UISubsystem subsystem = createSubsystemWithKey("key-encrypt-subsystem", "plain-test-key");
        assertTrue(isListedAsPlain(subsystem.getName()));

        subsystemDao.encryptSubsystemPrivateKey(subsystem.getId(), "v2:test");

        assertEquals("v2:test", subsystemDao.getSubsystemPrivateKey(subsystem.getName()));
        assertFalse(isListedAsPlain(subsystem.getName()));
    }

    private UISubsystem createSubsystemWithKey(String name, String privateKey) {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName(name);
        subsystem.setTitle("The Subsystem");
        subsystem.setCallbackUrl("http://no-such-host.dlm");
        subsystem.setLandingUrl("/landing-url");
        subsystem.setSubsystemUrl("/");
        subsystem.setPrivateKey(privateKey);
        subsystemDao.createSubsystem(subsystem);
        assertNotNull("ID must be generated", subsystem.getId());
        return subsystem;
    }

    private boolean isListedAsPlain(String name) {
        for (SubsystemPrivateKey key : subsystemDao.getSubsystemsWithPlainPrivateKey()) {
            if (name.equals(key.getName())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testGetSubsystemsAllowingToListUsers() {
        subsystemDao.getSubsystemsAllowingToListUsers();
    }
}
