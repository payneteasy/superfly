package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForGroup;
import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.group.UIGroupForView;

public interface GroupDao {

    @AStoredProcedure(name = "ui_get_groups_list")
    List<UIGroupForList> getGroups(long startFrom, long recordsCount,
            int orderFieldNumber, String orderType, String groupNamePrefix,
            String subsystemIds);

    @AStoredProcedure(name = "ui_create_group")
    RoutineResult createGroup(UIGroup group);

    @AStoredProcedure(name = "ui_delete_group")
    RoutineResult deleteGroup(long id);

    @AStoredProcedure(name = "ui_get_groups_list_count")
    long getGroupsCount(String groupName, String subsystemIds);

    @AStoredProcedure(name = "ui_get_group")
    UIGroupForView getGroupById(long id);

    @AStoredProcedure(name = "ui_edit_group_properties")
    RoutineResult updateGroup(long id, String groupName);

    @AStoredProcedure(name = "ui_change_group_actions")
    RoutineResult changeGroupActions(long groupId, String actionsToLink,
            String actionsToUnlink);

    @AStoredProcedure(name = "ui_get_mapped_group_actions_list")
    List<UIActionForCheckboxForGroup> getAllGroupMappedActions(long startFrom,
            long recordsCount, int orderFieldNumber, String orderType,
            long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_get_mapped_group_actions_list_count")
    long getAllGroupMappedActionsCount(long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_get_unmapped_group_actions_list")
    List<UIActionForCheckboxForGroup> getAllGroupUnMappedActions(int startFrom,
            int recordsCount, int orderFieldNumber, String orderType,
            long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_get_unmapped_group_actions_list_count")
    int getAllGroupUnMappedActionsCount(long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_get_all_group_actions_list")
    List<UIActionForCheckboxForGroup> getAllGroupActions(long startFrom,
            long recordsCount, int orderFieldNumber, String orderType,
            long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_get_all_group_actions_list_count")
    long getAllGroupActionsCount(long groupId, String actionNameSubstring);

    @AStoredProcedure(name = "ui_clone_group")
    RoutineResult cloneGroup(UICloneGroupRequest request);

}
