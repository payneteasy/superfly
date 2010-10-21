package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.SyslogService;

@Transactional
public class SubsystemServiceImpl implements SubsystemService {

	private static final Logger logger = LoggerFactory.getLogger(SubsystemServiceImpl.class);
	private static final org.apache.log4j.Logger apacheLogger = org.apache.log4j.Logger
			.getLogger(RoleServiceImpl.class);

	private SubsystemDao subsystemDao;
	private NotificationService notificationService;
	private LoggerSink loggerSink;
	private SyslogService syslogService;

	@Required
	public void setSubsystemDao(SubsystemDao subsystemDao) {
		this.subsystemDao = subsystemDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Required
	public void setLoggerSink(LoggerSink loggerSink) {
		this.loggerSink = loggerSink;
	}

	@Required
	public void setSyslogService(SyslogService syslogService) {
		this.syslogService = syslogService;
	}

	public RoutineResult createSubsystem(UISubsystem subsystem) {
		RoutineResult result = subsystemDao.createSubsystem(subsystem);
		loggerSink.info(logger, "CREATE_SUBSYSTEM", true, subsystem.getName());
		syslogService.sendLogMessage(apacheLogger, "CREATE_SUBSYSTEM", true, subsystem.getName());
		return result;
	}

	public RoutineResult deleteSubsystem(Long subsystemId) {
		RoutineResult result = subsystemDao.deleteSubsystem(subsystemId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "DELETE_SUBSYSTEM", result.isOk(), String.valueOf(subsystemId));
		syslogService.sendLogMessage(apacheLogger, "DELETE_SUBSYSTEM", result.isOk(), String.valueOf(subsystemId));
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
		loggerSink.info(logger, "UPDATE_SUBSYSTEM", result.isOk(), subsystem.getName());
		syslogService.sendLogMessage(apacheLogger, "UPDATE_SUBSYSTEM", result.isOk(), subsystem.getName());
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
