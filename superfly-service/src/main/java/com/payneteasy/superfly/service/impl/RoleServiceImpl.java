package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.RoleDao;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.service.RoleService;

@Transactional
public class RoleServiceImpl implements RoleService {
	
	private RoleDao roleDao;

	@Required
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public List<UIRoleForFilter> getRolesForFilter() {
		return roleDao.getRolesForFilter();
	}

	public List<UIRoleForCheckbox> getAllUserRoles(long userId) {
		// TODO: implement using a single DAO call when procedure is able to
		// return 'mapped' status
		List<UIRoleForCheckbox> allRoles = roleDao.getAllUserRoles(0,
				Integer.MAX_VALUE, 4 /*role_id*/, DaoConstants.ASC, userId);
		List<UIRoleForCheckbox> mappedRoles = roleDao.getMappedUserRoles(0,
				Integer.MAX_VALUE, 4 /*role_id*/, DaoConstants.ASC, userId);
		for (UIRoleForCheckbox mappedRole : mappedRoles) {
			int index = allRoles.indexOf(mappedRole);
			UIRoleForCheckbox role = allRoles.get(index);
			role.setMapped(true);
		}
		return allRoles;
	}

}
