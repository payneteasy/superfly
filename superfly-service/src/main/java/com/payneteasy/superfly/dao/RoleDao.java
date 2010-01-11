package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
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

	/**
	 * Returns a list of roles for the given user. Both assigned and
	 * not-assigned roles are returned.
	 * 
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @param orderFieldNumber
	 *            number of field to order by
	 * @param orderType
	 *            'asc'/'desc'
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_all_user_roles_list")
	List<UIRoleForCheckbox> getAllUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId);

	/**
	 * Returns a list of roles assigned to the given user.
	 * 
	 * @param startFrom
	 *            starting index for paging
	 * @param recordsCount
	 *            limit for paging
	 * @param orderFieldNumber
	 *            number of field to order by
	 * @param orderType
	 *            'asc'/'desc'
	 * @param userId
	 *            ID of the user whose roles are to be returned
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_get_mapped_user_roles_list")
	List<UIRoleForCheckbox> getMappedUserRoles(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, long userId);

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
}
