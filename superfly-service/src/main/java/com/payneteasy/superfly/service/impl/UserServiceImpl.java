package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.utils.StringUtils;

@Transactional
public class UserServiceImpl implements UserService {

	private UserDao userDao;

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
			Long complectId, Long subsystemId, int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc) {
		return userDao.getUsers(startFrom, recordsCount, orderFieldNumber,
				asc ? DaoConstants.ASC : DaoConstants.DESC, userNamePrefix,
				roleId, complectId, subsystemId);
	}

	public int getUsersCount(String userNamePrefix, Long roleId,
			Long complectId, Long subsystemId) {
		return userDao.getUsersCount(userNamePrefix, roleId, complectId,
				subsystemId);
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

	public List<UIActionForCheckboxForUser> getAllUserActions(long userId,
			List<Long> subsystemIds, String actionSubstring, int startFrom,
			int recordsCount, int orderFieldNumber, String orderType) {
		return userDao.getAllUserActions(startFrom, recordsCount,
				orderFieldNumber, orderType, userId, StringUtils
						.collectionToCommaDelimitedString(subsystemIds),
				actionSubstring);
	}

	public List<UIRoleForCheckbox> getAllUserRoles(long userId) {
		List<UIRoleForCheckbox> allRoles = userDao.getAllUserRoles(0,
				Integer.MAX_VALUE, 4 /* role_id */, DaoConstants.ASC, userId);
		return allRoles;
	}

	public void changeUserRoles(long userId, List<Long> rolesToAddIds,
			List<Long> rolesToRemoveIds) {
		userDao.changeUserRoles(userId, StringUtils
				.collectionToCommaDelimitedString(rolesToAddIds), StringUtils
				.collectionToCommaDelimitedString(rolesToRemoveIds));
	}

	public int getAllUserActionsCount(long userId, List<Long> subsystemIds,
			String actionSubstring) {
		return userDao.getAllUserActionsCount(userId, StringUtils
				.collectionToCommaDelimitedString(subsystemIds),
				actionSubstring);
	}

	public void changeUserRoleActions(long userId,
			List<Long> roleActionToAddIds, List<Long> roleActionToRemoveIds) {
		userDao
				.changeUserRoleActions(
						userId,
						StringUtils
								.collectionToCommaDelimitedString(roleActionToAddIds),
						StringUtils
								.collectionToCommaDelimitedString(roleActionToRemoveIds));
	}

}
