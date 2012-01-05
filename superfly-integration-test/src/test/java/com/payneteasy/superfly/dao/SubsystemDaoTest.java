package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServerForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import org.springframework.beans.factory.annotation.Autowired;

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

    public void testUpdateSubsystem(){
		UISubsystem subsystem = getAnySubsystem();
		subsystem.setName("testName");
		subsystem.setCallbackInformation("testCallbackInfo");
		RoutineResult result = subsystemDao.updateSubsystem(subsystem);
		assertRoutineResult(result);
	}
	
	private UISubsystem getAnySubsystem(){
		long subsystemId=getAnySubsystemId();
		UISubsystem subsystem = subsystemDao.getSubsystem(subsystemId);
		return subsystem;
	}
	
	private long getAnySubsystemId(){
		List<UISubsystemForList> subsystems = subsystemDao.getSubsystems();
		UISubsystemForList subsystemForList = subsystems.get(0);
		long subsystemId=subsystemForList.getId();
		return subsystemId;	
	}
	
	private String getAnySubsystemName(){
		List<UISubsystemForList> subsystems = subsystemDao.getSubsystems();
		UISubsystemForList subsystemForList = subsystems.get(0);
		String subsystemName = subsystemForList.getName();
		return subsystemName;	
	}
	
	public void testGetSubsystems() {
		List<UISubsystemForList> list = subsystemDao.getSubsystems();
		assertNotNull("Subsystems list should not be null", list);
		assertTrue("Subsystems list should not be empty", list.size() > 0);
	}
	
	public void testCreateSubsystem() {
        UISmtpServer server = new UISmtpServer();
        server.setName("for-addition-to-subsystem");
        server.setHost("host");
        smtpServerDao.createSmtpServer(server);

        // no smtp server
		UISubsystem subsystem = new UISubsystem();
		subsystem.setName("subsystem-name");
		subsystem.setCallbackInformation("http://no-such-host.dlm");
		subsystemDao.createSubsystem(subsystem);
		assertNotNull("ID must be generated", subsystem.getId());

        // with smtp server
        UISmtpServerForFilter serverForFilter = new UISmtpServerForFilter();
        serverForFilter.setId(server.getId());
        serverForFilter.setName(server.getName());

		subsystem = new UISubsystem();
		subsystem.setName("subsystem-name-2");
		subsystem.setCallbackInformation("http://no-such-host.dlm");
        subsystem.setSmtpServer(serverForFilter);
		subsystemDao.createSubsystem(subsystem);
		assertNotNull("ID must be generated", subsystem.getId());
	}
	
	public void testGetSubsystemsForFilter() {
		List<UISubsystemForFilter> list = subsystemDao.getSubsystemsForFilter();
		assertNotNull("Subsystems list should not be null", list);
		assertTrue("Subsystems list should not be empty", list.size() > 0);
	}
	
	public void testGetSubsystem() {
		subsystemDao.getSubsystem(getAnySubsystemId());
	}
	
	public void testGetSubsystemByName() {
		subsystemDao.getSubsystemByName(getAnySubsystemName());
	}
	
	public void testGetSubsystemsAllowingToListUsers() {
		subsystemDao.getSubsystemsAllowingToListUsers();
	}
}
