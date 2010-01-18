package com.payneteasy.superfly.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.UserService;

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
	
	public List<UIRoleForCheckbox> getAllUserRoles(long userId,
			Long subsystemId, int startFrom, int recordsCount) {
		List<UIRoleForCheckbox> allRoles = userDao.getAllUserRoles(startFrom,
				recordsCount, 4 /* role_id */, DaoConstants.ASC, userId,
				subsystemId == null ? null : String.valueOf(subsystemId));
		return allRoles;
	}
	
	public int getAllUserRolesCount(long userId, Long subsystemId) {
		return userDao.getAllUserRolesCount(userId,
				subsystemId == null ? null : String.valueOf(subsystemId));
	}
	
	public List<UIRoleForCheckbox> getUnmappedUserRoles(long userId,
			Long subsystemId, int startFrom, int recordsCount) {
		List<UIRoleForCheckbox> allRoles = userDao.getUnmappedUserRoles(startFrom,
				recordsCount, 4 /* role_id */, DaoConstants.ASC, userId,
				subsystemId == null ? null : String.valueOf(subsystemId));
		return allRoles;
	}
	
	public int getUnmappedUserRolesCount(long userId, Long subsystemId) {
		return userDao.getUnmappedUserRolesCount(userId,
				subsystemId == null ? null : String.valueOf(subsystemId));
	}

	public void changeUserRoles(long userId, Collection<Long> rolesToAddIds,
			Collection<Long> rolesToRemoveIds,
			Collection<Long> rolesToGrantActionsIds) {
		rolesToGrantActionsIds = new HashSet<Long>(rolesToGrantActionsIds);
		rolesToGrantActionsIds.retainAll(rolesToAddIds);
		userDao.changeUserRoles(userId,
				StringUtils.collectionToCommaDelimitedString(rolesToAddIds),
				StringUtils.collectionToCommaDelimitedString(rolesToRemoveIds),
				StringUtils.collectionToCommaDelimitedString(rolesToGrantActionsIds));
	}

	public List<UIActionForCheckboxForUser> getAllUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getAllUserActions(startFrom, recordsCount,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				userId, subsystemIds, actionSubstring);
	}

	public int getAllUserActionsCount(long userId, Long subsystemId,
			String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getAllUserActionsCount(userId, subsystemIds, actionSubstring);
	}
	
	public List<UIActionForCheckboxForUser> getUnmappedUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getUnmappedUserActions(startFrom, recordsCount,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				userId, subsystemIds, actionSubstring);
	}

	public int getUnmappedUserActionsCount(long userId, Long subsystemId,
			String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getUnmappedUserActionsCount(userId, subsystemIds, actionSubstring);
	}

	public void changeUserRoleActions(long userId,
			Collection<Long> roleActionToAddIds,
			Collection<Long> roleActionToRemoveIds) {
		userDao.changeUserRoleActions(userId,
				StringUtils.collectionToCommaDelimitedString(roleActionToAddIds),
				StringUtils.collectionToCommaDelimitedString(roleActionToRemoveIds));
	}

	public UIUserWithRolesAndActions getUserRoleActions(long userId,
			String subsystemIds, String actionNameSubstring,
			String roleNameSubstring) {
		return userDao.getUserRoleActions(userId, subsystemIds, actionNameSubstring, roleNameSubstring);
	}

}
