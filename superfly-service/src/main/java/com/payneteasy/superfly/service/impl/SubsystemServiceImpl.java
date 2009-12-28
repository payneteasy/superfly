package com.payneteasy.superfly.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.SubsystemService;

@Transactional
public class SubsystemServiceImpl implements SubsystemService {
	
	private SubsystemDao subsystemDao;

	@Required
	public void setSubsystemDao(SubsystemDao subsystemDao) {
		this.subsystemDao = subsystemDao;
	}

	public void createSubsystem(UISubsystem subsystem) {
		subsystemDao.createSubsystem(subsystem);
	}

	public void deleteSubsystem(Long subsystemId) {
		subsystemDao.deleteSubsystem(subsystemId);
	}

	public List<UISubsystemForList> getSubsystems() {
		return subsystemDao.getSubsystems();
	}

	public void updateSubsystem(UISubsystem subsystem) {
		// TODO Auto-generated method stub

	}

	public List<UISubsystemForFilter> getSubsystemsForFilter() {
		return subsystemDao.getSubsystemsForFilter();
	}

}
