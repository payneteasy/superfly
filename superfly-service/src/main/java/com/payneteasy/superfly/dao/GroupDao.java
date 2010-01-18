package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;

public interface GroupDao {

	@AStoredProcedure(name = "ui_get_groups_list")
	List<UIGroupForList> getGroups(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, String groupNamePrefix,
			String subsystemIds);

	@AStoredProcedure(name = "ui_create_group")
	RoutineResult createGroup(UIGroup group);

	@AStoredProcedure(name = "ui_delete_group")
	RoutineResult deleteGorup(long id);
	
	@AStoredProcedure(name = "ui_get_groups_list_count")
	int getGroupsCount(String groupName, String subsystemIds);
	
	@AStoredProcedure(name = "ui_get_group")
	UIGroup getGroupById(long id);
	
	@AStoredProcedure(name = "ui_edit_group_properties")
	RoutineResult updateGroup(long id, String groupName);

	@AStoredProcedure(name = "ui_change_group_actions")
	RoutineResult changeGroupActions(long groupId, String actionsToLink, String actionsToUnlink);
	
	@AStoredProcedure(name = "ui_get_mapped_group_actions_list")
	List<UIActionForCheckboxForGroup> getAllGroupMappedActions(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType,
			long groupId, String actionNameSubstring);
	
	@AStoredProcedure(name = "ui_get_mapped_group_actions_list_count")
	int getAllGroupMappedActionsCount(long groupId, String actionNameSubstring);
	
}
