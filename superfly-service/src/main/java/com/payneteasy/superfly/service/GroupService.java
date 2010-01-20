package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.group.UIGroupForView;

public interface GroupService {
	List<UIGroupForList> getGroups();
	List<UIGroupForList> getGroupsForSubsystems(int startFrom, int recordsCount,
			int orderFieldNumber, boolean orderType, String groupNamePrefix,
			List<Long> subsystemIds);
	void createGroup(UIGroup group);
	
	void updateGroup(UIGroup group);
	
	void deleteGroup(long id);
	
	void cloneGroup(UICloneGroupRequest request);
	
	UIGroupForView getGroupById(long id);
	
	int getGroupsCount(String groupName, List<Long> subsystemIds);
	
	void changeGroupActions(long groupId, List<Long> ActionsToLink, List<Long> ActionsToUnlink);
	
	List<UIActionForCheckboxForGroup> getAllGroupMappedActions(int stratFrom,
			int recordsCount, int orderFieldNumber, boolean orderType, long groupId, String actionSubstring);
	
	int getAllGroupMappedActionsCount(long groupId, String actionSubstring);
	
	List<UIActionForCheckboxForGroup> getAllGroupActions(int stratFrom,
			int recordsCount, int orderFieldNumber, boolean orderType, long groupId, String actionSubstring);
	
	int getAllGroupActionsCount(long groupId, String actionSubstring);
	
	


}
