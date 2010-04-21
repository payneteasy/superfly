package com.payneteasy.superfly.service.impl;

import java.util.List;

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
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class GroupServiceImpl implements GroupService {
	private GroupDao groupDao;
	private NotificationService notificationService;

	@Required
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public List<UIGroupForList> getGroups() {
		return this.groupDao.getGroups(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				DaoConstants.ASC, null, null);
	}
	
	public RoutineResult createGroup(UIGroup group) {
		RoutineResult result = groupDao.createGroup(group);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public RoutineResult deleteGroup(long id) {
		RoutineResult result = groupDao.deleteGorup(id);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public List<UIGroupForList> getGroupsForSubsystems(int startFrom,
			int recordsCount, int orderFieldNumber, boolean orderType,
			String groupNamePrefix, List<Long> subsystemIds) {
		return this.groupDao.getGroups(startFrom, recordsCount,
				orderFieldNumber,
				orderType ? DaoConstants.ASC : DaoConstants.DESC,
				groupNamePrefix, 
				StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public int getGroupsCount(String groupName, List<Long> subsystemIds) {
		return groupDao.getGroupsCount(groupName, StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public UIGroupForView getGroupById(long id) {
		return groupDao.getGroupById(id);
	}

	public RoutineResult updateGroup(UIGroup group) {
		return groupDao.updateGroup(group.getId(), group.getName());
	}

	public RoutineResult changeGroupActions(long groupId, List<Long> ActionsToLink,
			List<Long> ActionsToUnlink) {
		RoutineResult result = groupDao.changeGroupActions(groupId,
						StringUtils.collectionToCommaDelimitedString(ActionsToLink),
						StringUtils.collectionToCommaDelimitedString(ActionsToUnlink));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;		
	}

	public List<UIActionForCheckboxForGroup> getAllGroupMappedActions(int startFrom,
			int recordsCount, int orderFieldNumber, boolean orderType,
			long groupId, String actionSubstring) {
		return groupDao.getAllGroupMappedActions(startFrom, recordsCount, 
				orderFieldNumber, 
				orderType ? DaoConstants.ASC : DaoConstants.DESC, 
				groupId, actionSubstring);
	}
	
	public int getAllGroupMappedActionsCount(long groupId, String actionSubstring) {
		return groupDao.getAllGroupMappedActionsCount(groupId, actionSubstring);
	}
	
	public List<UIActionForCheckboxForGroup> getAllGroupActions(int startFrom,
			int recordsCount, int orderFieldNumber, boolean orderType,
			long groupId, String actionSubstring) {
		return groupDao.getAllGroupActions(startFrom, recordsCount, 
				orderFieldNumber, 
				orderType ? DaoConstants.ASC : DaoConstants.DESC, 
				groupId, actionSubstring);
	}
	
	public int getAllGroupActionsCount(long groupId, String actionSubstring) {
		return groupDao.getAllGroupActionsCount(groupId, actionSubstring);
	}

	public RoutineResult cloneGroup(UICloneGroupRequest request) {
		RoutineResult result = groupDao.cloneGroup(request);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public List<UIActionForCheckboxForGroup> getAllGroupUnMappedActions(
			int startFrom, int recordsCount, int orderFieldNumber,
			boolean orderType, long groupId, String actionSubstring) {
		return groupDao.getAllGroupUnMappedActions(startFrom, recordsCount, 
				orderFieldNumber, 
				orderType ? DaoConstants.ASC : DaoConstants.DESC, 
				groupId, actionSubstring);
	}

	public int getAllGroupUnMappedActionsCount(long groupId,
			String actionSubstring) {
		return groupDao.getAllGroupUnMappedActionsCount(groupId, actionSubstring);
	}

}
