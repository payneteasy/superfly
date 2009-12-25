package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;

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
}
