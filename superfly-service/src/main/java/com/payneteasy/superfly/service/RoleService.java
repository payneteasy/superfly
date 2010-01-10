package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;

public interface RoleService {
	/**
	 * Returns list of roles for UI filter.
	 * 
	 * @return roles
	 */
	List<UIRoleForFilter> getRolesForFilter();

	/**
	 * Returns a list of roles for the given user. Each role is 'mapped' or
	 * 'unmapped' depending on whether it's assigned this user or not.
	 * 
	 * @param userId
	 *            ID of the user
	 * @return list of roles
	 */
	List<UIRoleForCheckbox> getAllUserRoles(long userId);

	List<UIRoleForList> getRoles(int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc, String rolesName,
			List<Long> subsystems);

	int getRoleCount(String rolesName, List<Long> subsystems);

	void deleteRole(long roleId);

	UIRole getRole(long roleId);

	void updateRole(UIRole role);
}
