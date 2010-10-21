package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.GroupDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.group.UIGroupForView;
import com.payneteasy.superfly.service.GroupService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SyslogService;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class GroupServiceImpl implements GroupService {

	private Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);
	private static final org.apache.log4j.Logger apacheLogger = org.apache.log4j.Logger
			.getLogger(GroupServiceImpl.class);

	private GroupDao groupDao;
	private NotificationService notificationService;
	private LoggerSink loggerSink;
	private SyslogService syslogService;

	@Required
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
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

	public List<UIGroupForList> getGroups() {
		return this.groupDao.getGroups(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC, null, null);
	}

	public RoutineResult createGroup(UIGroup group) {
		RoutineResult result = groupDao.createGroup(group);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CREATE_GROUP", result.isOk(), group.getName());
		syslogService.sendLogMessage(apacheLogger, "CREATE_GROUP", result.isOk(), group.getName());
		return result;
	}

	public RoutineResult deleteGroup(long id) {
		RoutineResult result = groupDao.deleteGroup(id);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "DELETE_GROUP", result.isOk(), String.valueOf(id));
		syslogService.sendLogMessage(apacheLogger, "DELETE_GROUP", result.isOk(), String.valueOf(id));
		return result;
	}

	public List<UIGroupForList> getGroupsForSubsystems(int startFrom, int recordsCount, int orderFieldNumber,
			boolean orderType, String groupNamePrefix, List<Long> subsystemIds) {
		return this.groupDao.getGroups(startFrom, recordsCount, orderFieldNumber, orderType ? DaoConstants.ASC
				: DaoConstants.DESC, groupNamePrefix, StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public int getGroupsCount(String groupName, List<Long> subsystemIds) {
		return groupDao.getGroupsCount(groupName, StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public UIGroupForView getGroupById(long id) {
		return groupDao.getGroupById(id);
	}

	public RoutineResult updateGroup(UIGroup group) {
		RoutineResult result = groupDao.updateGroup(group.getId(), group.getName());
		loggerSink.info(logger, "UPDATE_GROUP", result.isOk(), group.getName());
		syslogService.sendLogMessage(apacheLogger, "UPDATE_GROUP", result.isOk(), group.getName());
		return result;
	}

	public RoutineResult changeGroupActions(long groupId, List<Long> actionsToLink, List<Long> actionsToUnlink) {
		RoutineResult result = groupDao.changeGroupActions(groupId,
				StringUtils.collectionToCommaDelimitedString(actionsToLink),
				StringUtils.collectionToCommaDelimitedString(actionsToUnlink));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CHANGE_GROUP_ACTIONS", result.isOk(), String.valueOf(groupId));
		syslogService.sendLogMessage(apacheLogger, "CHANGE_GROUP_ACTIONS", result.isOk(), String.valueOf(groupId));
		return result;
	}

	public List<UIActionForCheckboxForGroup> getAllGroupMappedActions(int startFrom, int recordsCount,
			int orderFieldNumber, boolean orderType, long groupId, String actionSubstring) {
		return groupDao.getAllGroupMappedActions(startFrom, recordsCount, orderFieldNumber,
				orderType ? DaoConstants.ASC : DaoConstants.DESC, groupId, actionSubstring);
	}

	public int getAllGroupMappedActionsCount(long groupId, String actionSubstring) {
		return groupDao.getAllGroupMappedActionsCount(groupId, actionSubstring);
	}

	public List<UIActionForCheckboxForGroup> getAllGroupActions(int startFrom, int recordsCount, int orderFieldNumber,
			boolean orderType, long groupId, String actionSubstring) {
		return groupDao.getAllGroupActions(startFrom, recordsCount, orderFieldNumber, orderType ? DaoConstants.ASC
				: DaoConstants.DESC, groupId, actionSubstring);
	}

	public int getAllGroupActionsCount(long groupId, String actionSubstring) {
		return groupDao.getAllGroupActionsCount(groupId, actionSubstring);
	}

	public RoutineResult cloneGroup(UICloneGroupRequest request) {
		RoutineResult result = groupDao.cloneGroup(request);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CLONE_GROUP", result.isOk(),
				String.format("%s->%s", request.getSourceGroupId(), request.getNewGroupName()));
		syslogService.sendLogMessage(apacheLogger, "CLONE_GROUP", result.isOk(),
				String.format("%s->%s", request.getSourceGroupId(), request.getNewGroupName()));
		return result;
	}

	public List<UIActionForCheckboxForGroup> getAllGroupUnMappedActions(int startFrom, int recordsCount,
			int orderFieldNumber, boolean orderType, long groupId, String actionSubstring) {
		return groupDao.getAllGroupUnMappedActions(startFrom, recordsCount, orderFieldNumber,
				orderType ? DaoConstants.ASC : DaoConstants.DESC, groupId, actionSubstring);
	}

	public int getAllGroupUnMappedActionsCount(long groupId, String actionSubstring) {
		return groupDao.getAllGroupUnMappedActionsCount(groupId, actionSubstring);
	}

}
