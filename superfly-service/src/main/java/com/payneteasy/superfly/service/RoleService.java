package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;

public interface RoleService {
	/**
	 * Returns list of roles for UI filter.
	 * 
	 * @return roles
	 */
	List<UIRoleForFilter> getRolesForFilter();
}
