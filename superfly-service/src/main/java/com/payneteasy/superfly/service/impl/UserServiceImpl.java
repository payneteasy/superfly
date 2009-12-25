package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.service.UserService;

@Transactional
public class UserServiceImpl implements UserService {
	
	private UserDao userDao;

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
			Long complectId, int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc) {
		return userDao.getUsers(startFrom, recordsCount, orderFieldNumber,
				asc ? DaoConstants.ASC : DaoConstants.DESC, userNamePrefix,
				roleId, complectId);
	}

	public int getUsersCount(String userNamePrefix, Long roleId, Long complectId) {
		return userDao.getUsersCount(userNamePrefix, roleId, complectId);
	}

}
