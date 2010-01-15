package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.GroupDao;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.service.*;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class GroupServiceImpl implements GroupService {
	private GroupDao groupDao;

	@Required
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public List<UIGroupForList> getGroups() {
		return this.groupDao.getGroups(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				DaoConstants.ASC, null, null);
	}
	
	

	public void createGroup(UIGroup group) {
		groupDao.createGroup(group);
		
	}

	public void deleteGroup(long id) {
		groupDao.deleteGorup(id);
		
	}

	public List<UIGroupForList> getGroupsForSubsystems(int startFrom,
			int recordsCount, int orderFieldNumber, boolean orderType,
			String groupNamePrefix, List<Long> subsystemIds) {
		return this.groupDao.getGroups(
				startFrom, 
				recordsCount, 
				orderFieldNumber,
				orderType ? DaoConstants.ASC: DaoConstants.DESC, 
				groupNamePrefix, 
				StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public int getGroupsCount(String groupName, List<Long> subsystemIds) {
		return groupDao.getGroupsCount(groupName, StringUtils.collectionToCommaDelimitedString(subsystemIds));
	}

	public UIGroup getGroupById(long id) {
		return groupDao.getGroupById(id);
		
	}

	public void updateGroup(UIGroup group) {
		groupDao.updateGroup(group.getId(), group.getName());
		
	}

	public void changeGroupActions(long groupId, List<Long> ActionsToLink,
			List<Long> ActionsToUnlink) {
			groupDao.changeGroupActions(
					groupId,
					StringUtils.collectionToCommaDelimitedString(ActionsToLink),
					StringUtils.collectionToCommaDelimitedString(ActionsToUnlink));
		
	}

}
