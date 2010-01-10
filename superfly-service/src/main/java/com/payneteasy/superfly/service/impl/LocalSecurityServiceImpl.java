package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.service.LocalSecurityService;

@Transactional
public class LocalSecurityServiceImpl implements LocalSecurityService {
	
	private UserDao userDao;
	private String localSubsystemName = "superfly";
	private String localRoleName = "admin";

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public void setLocalSubsystemName(String localSubsystemName) {
		this.localSubsystemName = localSubsystemName;
	}

	public void setLocalRoleName(String localRoleName) {
		this.localRoleName = localRoleName;
	}

	public String[] authenticate(String username, String password) {
		List<AuthRole> roles = userDao.authenticate(username, password,
				localSubsystemName, null, null);
		if (roles != null) {
			AuthRole role = null;
			for (AuthRole r : roles) {
				if (localRoleName.equals(r.getRoleName())) {
					role = r;
					break;
				}
			}
			if (role != null) {
				String[] result = new String[role.getActions().size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = role.getActions().get(i).getActionName();
				}
				return result;
			}
		}
		return null;
	}

}
