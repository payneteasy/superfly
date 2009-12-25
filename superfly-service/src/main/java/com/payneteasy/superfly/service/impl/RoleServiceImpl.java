package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.RoleDao;
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

}
