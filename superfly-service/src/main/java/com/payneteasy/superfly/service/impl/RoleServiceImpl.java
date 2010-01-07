package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.RoleDao;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.service.RoleService;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class RoleServiceImpl implements RoleService {

	private RoleDao roleDao;

	@Required
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public List<UIRoleForFilter> getRolesForFilter() {
		return roleDao.getRolesForFilter(null, null, 0, Integer.MAX_VALUE);
	}

	public List<UIRoleForCheckbox> getAllUserRoles(long userId) {
		List<UIRoleForCheckbox> allRoles = roleDao.getAllUserRoles(0,
				Integer.MAX_VALUE, 4 /* role_id */, DaoConstants.ASC, userId);
		return allRoles;
	}

	public int getRoleCount(String rolesName, List<Long> subsystems) {
		return roleDao.getRoleCount(rolesName, StringUtils.collectionToCommaDelimitedString(subsystems));
	}

	public List<UIRoleForList> getRoles(int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc, String rolesName,
			List<Long> subsystems) {

		return roleDao.getRoles(startFrom, recordsCount, orderFieldNumber,
				asc ? DaoConstants.ASC : DaoConstants.DESC, rolesName,
				StringUtils.collectionToCommaDelimitedString(subsystems));
	}

	public void deleteRole(long roleId) {
		roleDao.deleteRole(roleId);
	}
	

}
