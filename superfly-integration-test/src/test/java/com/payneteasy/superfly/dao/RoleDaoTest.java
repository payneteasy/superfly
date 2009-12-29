package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.user.UIUserForList;

public class RoleDaoTest extends AbstractDaoTest {
	private RoleDao roleDao;
	private UserDao userDao;

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void testGetRolesForFilter() {
		List<UIRoleForFilter> roles = roleDao.getRolesForFilter();
		assertNotNull("List of roles must not be null", roles);
		assertTrue("List of roles cannot be empty", roles.size() > 0);
	}
	
	public void testGetAllUserRoles() {
		roleDao.getMappedUserRoles(0, 10, 1, "asc", getAnyUserId());
	}
	
	public void testGetMappedUserRoles() {
		roleDao.getMappedUserRoles(0, 10, 1, "asc", getAnyUserId());
	}
	
	private long getAnyUserId() {
		List<UIUserForList> users = userDao.getUsers(0, 1, 1, "asc", null, null,
				null, null);
		UIUserForList userForList = users.get(0);
		long userId = userForList.getId();
		return userId;
	}
}
