package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
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

	public void createUser(UIUser user) {
		userDao.createUser(user);
	}

	public UIUser getUser(long userId) {
		return userDao.getUser(userId);
	}

	public void updateUser(UIUser user) {
		userDao.updateUser(user);
	}
	
	public void deleteUser(long userId) {
		userDao.deleteUser(userId);
	}

	public void lockUser(long userId) {
		userDao.lockUser(userId);
	}

	public void unlockUser(long userId) {
		userDao.unlockUser(userId);
	}

	public long cloneUser(long templateUserId, String newUsername,
			String newPassword) {
		UICloneUserRequest request = new UICloneUserRequest();
		request.setTemplateUserId(templateUserId);
		request.setUsername(newUsername);
		request.setPassword(newPassword);
		userDao.cloneUser(request);
		return request.getId();
	}

}
