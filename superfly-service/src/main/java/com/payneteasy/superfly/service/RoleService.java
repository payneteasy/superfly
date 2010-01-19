package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForRole;
import com.payneteasy.superfly.model.ui.group.UIGroupForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;

public interface RoleService {
	/**
	 * Returns list of roles for UI filter.
	 * 
	 * @return roles
	 */
	List<UIRoleForFilter> getRolesForFilter();

	List<UIRoleForList> getRoles(int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc, String rolesName,
			List<Long> subsystems);

	int getRoleCount(String rolesName, List<Long> subsystems);

	void deleteRole(long roleId);

	UIRole getRole(long roleId);

	void updateRole(UIRole role);

	void createRole(UIRole role);

	List<UIGroupForCheckbox> getAllRoleGroups(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long roleId);

	void changeRoleGroups(long roleId, List<Long> groupToAddIds,
			List<Long> groupToRemoveIds);

	List<UIActionForCheckboxForRole> getAllRoleActions(int startFrom,
			int recordsCount, int orderFieldNumber, boolean ascending,
			long roleId, String actionName);

	int getAllRoleActionsCount(long roleId, String actionName);
	
	List<UIActionForCheckboxForRole> getMappedRoleActions(int startFrom,
			int recordsCount, int orderFieldNumber, boolean ascending,
			long roleId, String actionName);

	int getMappedRoleActionsCount(long roleId, String actionName);

	int getAllRoleGroupsCount(long roleId);

	void changeRoleActions(long roleId, List<Long> actionToAddIds,
			List<Long> actionToRemoveIds);
}
