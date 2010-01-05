package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
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
	 * @return roles
	 */
	@AStoredProcedure(name = "ui_filter_roles")
	List<UIRoleForFilter> getRolesForFilter();

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
}
