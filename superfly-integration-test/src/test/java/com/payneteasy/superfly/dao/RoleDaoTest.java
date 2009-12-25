package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;

public class RoleDaoTest extends AbstractDaoTest {
	private RoleDao roleDao;

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
	
	public void testGetRolesForFilter() {
		List<UIRoleForFilter> roles = roleDao.getRolesForFilter();
		assertNotNull("List of roles must not be null", roles);
		assertTrue("List of roles cannot be empty", roles.size() > 0);
	}
}
