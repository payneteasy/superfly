package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;

/**
 * DAO to work with roles.
 * 
 * @author Roman Puchkovskiy
 */
public interface RoleDao {
	/**
	 * Returns list of roles for UI filter.
	 * 
	 * @param subsystemIds
	 *            comma-separated list of IDs of subsystems to consider (ignored
	 *            if null)
	 * @param rolePrefix
	 *            prefix from which role name must start (ignored if null)
	 * @param startFrom
	 *            role offset
	 * @param recordsCount
	 *            limit
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_filter_dyn_roles")
	List<UIRoleForFilter> getRolesForFilter(String subsystemIds,
			String rolePrefix, int startFrom, int recordsCount);

	@AStoredProcedure(name = "ui_get_roles_list")
	List<UIRoleForList> getRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, String rolesName,
			String subsystemsName);

	@AStoredProcedure(name = "ui_get_roles_list_count")
	int getRoleCount(String rolesName, String subsystemsName);

	@AStoredProcedure(name = "ui_get_role")
	UIRole getRole(long roleId);

	@AStoredProcedure(name = "ui_delete_role")
	RoutineResult deleteRole(long roleId);

	@AStoredProcedure(name = "ui_edit_role_properties")
	RoutineResult updateRole(UIRole role);
	
	@AStoredProcedure(name = "ui_create_role")
	RoutineResult createRole(UIRole role);
	
	@AStoredProcedure(name = "ui_change_role_groups")
	RoutineResult changeRoleGroups(long roleId, String groupToAddIds,String groupToRemoveIds);
}
