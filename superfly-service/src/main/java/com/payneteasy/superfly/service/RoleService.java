package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.role.UIRoleForView;

public interface RoleService {
	/**
	 * Returns list of roles for UI filter.
	 * 
	 * @return roles
	 */
	List<UIRoleForFilter> getRolesForCreateUser(List<Long> subId);

	List<UIRoleForFilter> getRolesForFilter();

	List<UIRoleForList> getRoles(long startFrom, long recordsCount,
			int orderFieldNumber, boolean asc, String rolesName,
			List<Long> subsystems);

    long getRoleCount(String rolesName, List<Long> subsystems);

	RoutineResult deleteRole(long roleId);

	UIRoleForView getRole(long roleId);

	RoutineResult updateRole(UIRole role);

	RoutineResult createRole(UIRole role);

	List<UIGroupForCheckbox> getAllRoleGroups(long startFrom, long recordsCount,
			int orderFieldNumber, String orderType, long roleId);

	RoutineResult changeRoleGroups(long roleId, List<Long> groupToAddIds,
			List<Long> groupToRemoveIds);

	List<UIActionForCheckboxForRole> getAllRoleActions(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
			long roleId, String actionName);

    long getAllRoleActionsCount(long roleId, String actionName);

	List<UIActionForCheckboxForRole> getMappedRoleActions(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
			long roleId, String actionName);

	List<UIActionForCheckboxForRole> getUnMappedRoleActions(long startFrom,
            long recordsCount, int orderFieldNumber, boolean ascending,
			long roleId, String actionName);

    long getMappedRoleActionsCount(long roleId, String actionName);

    long getAllRoleGroupsCount(long roleId);

	RoutineResult changeRoleActions(long roleId, List<Long> actionToAddIds,
			List<Long> actionToRemoveIds);

	List<UIGroupForCheckbox> getMappedRoleGroups(long startFrom,
			long recordsCount, int orderFieldNumber, boolean ascending,
			long roleId);

    int getMappedRoleGroupsCount(long roleId);

	List<UIGroupForCheckbox> getUnMappedRoleGroups(long startFrom,
			long recordsCount, int orderFieldNumber, boolean ascending,
			long roleId);
}
