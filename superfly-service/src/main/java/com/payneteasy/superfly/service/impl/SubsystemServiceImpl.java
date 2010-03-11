package com.payneteasy.superfly.service.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SubsystemService;

@Transactional
public class SubsystemServiceImpl implements SubsystemService {
	
	private SubsystemDao subsystemDao;
	private NotificationService notificationService;

	@Required
	public void setSubsystemDao(SubsystemDao subsystemDao) {
		this.subsystemDao = subsystemDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public RoutineResult createSubsystem(UISubsystem subsystem) {
		return subsystemDao.createSubsystem(subsystem);
	}

	public RoutineResult deleteSubsystem(Long subsystemId) {
		RoutineResult result = subsystemDao.deleteSubsystem(subsystemId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public List<UISubsystemForList> getSubsystems() {
		return subsystemDao.getSubsystems();
	}

	public RoutineResult updateSubsystem(UISubsystem subsystem) {
		RoutineResult result = subsystemDao.updateSubsystem(subsystem);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public List<UISubsystemForFilter> getSubsystemsForFilter() {
		return subsystemDao.getSubsystemsForFilter();
	}

	public UISubsystem getSubsystem(long subsystemId) {
		return subsystemDao.getSubsystem(subsystemId);
	}

	public UISubsystem getSubsystemByName(String subsystemName) {
		return subsystemDao.getSubsystemByName(subsystemName);
	}

}
