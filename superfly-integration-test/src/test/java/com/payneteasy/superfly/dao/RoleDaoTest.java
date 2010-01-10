package com.payneteasy.superfly.dao;

import java.util.List;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
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

	public void testUpdateRole() {
		UIRole role = getAnyRole();
		role.setPrincipalName("principalNameTest");
		RoutineResult result=roleDao.updateRole(role);
		assertRoutineResult(result);
	}

	private UIRole getAnyRole() {
		long roleId = getAnyRoleId();
		UIRole role = roleDao.getRole(roleId);
		return role;
	}

	public void testGetRolesForFilter() {
		List<UIRoleForFilter> roles = roleDao.getRolesForFilter(null, null, 0,
				Integer.MAX_VALUE);
		assertNotNull("List of roles must not be null", roles);
		assertTrue("List of roles cannot be empty", roles.size() > 0);
	}

	public void testGetAllUserRoles() {
		roleDao.getAllUserRoles(0, 10, 1, "asc", getAnyUserId());
	}

	public void testGetMappedUserRoles() {
		roleDao.getMappedUserRoles(0, 10, 1, "asc", getAnyUserId());
	}

	private long getAnyUserId() {
		List<UIUserForList> users = userDao.getUsers(0, 1, 1, "asc", null,
				null, null, null);
		UIUserForList userForList = users.get(0);
		long userId = userForList.getId();
		return userId;
	}

	public void testGetRolesForList() {
		List<UIRoleForList> roles;
		roles = roleDao.getRoles(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", null, null);
		assertNotNull("List cannot be null", roles);
		roles = roleDao.getRoles(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", "someRoleName", "1,2");
		assertNotNull("List cannot be null", roles);
	}

	public void testGetRoleCount() {
		int count = roleDao.getRoleCount(null, null);
		assertTrue("Must get some roles", count > 0);
		roleDao.getRoleCount("someRoleName", "1,2");
	}

	private long getAnyRoleId() {
		List<UIRoleForList> roles = roleDao
				.getRoles(0, 1, 1, "asc", null, null);
		UIRoleForList roleForList = roles.get(0);
		long roleId = roleForList.getId();
		return roleId;
	}

	public void testDeleteRole() {
		long roleId = getAnyRoleId();
		RoutineResult result = roleDao.deleteRole(roleId);
		assertRoutineResult(result);
	}
}
