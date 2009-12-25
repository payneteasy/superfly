package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;

public class SubsystemDaoTest extends AbstractDaoTest {
	private SubsystemDao subsystemDao;

	public void setSubsystemDao(SubsystemDao subsystemDao) {
		this.subsystemDao = subsystemDao;
	}
	
	public void testGetSubsystems() {
		List<UISubsystemForList> list = subsystemDao.getSubsystems();
		assertNotNull("Subsystems list should not be null", list);
		assertTrue("Subsystems list should not be empty", list.size() > 0);
	}
	
	public void testCreateSubsystem() {
		UISubsystem subsystem = new UISubsystem();
		subsystem.setName("subsystem-name");
		subsystem.setIdentifier("subsystem-identifier");
		subsystem.setCallbackInformation("http://no-such-host.dlm");
		subsystemDao.createSubsystem(subsystem);
		assertNotNull("ID must be generated", subsystem.getId());
	}
	
	public void testGetSubsystemsForFilter() {
		List<UISubsystemForFilter> list = subsystemDao.getSubsystemsForFilter();
		assertNotNull("Subsystems list should not be null", list);
		assertTrue("Subsystems list should not be empty", list.size() > 0);
	}
}
