package com.payneteasy.superfly.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.UserService;

@Transactional
public class UserServiceImpl implements UserService {
	
	private UserDao userDao;
	private NotificationService notificationService;

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
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

	public RoutineResult createUser(UIUser user) {
		return userDao.createUser(user);
		// we're not notifying about this as user does not yet have any roles
		// or actions
	}

	public UIUser getUser(long userId) {
		return userDao.getUser(userId);
	}

	public RoutineResult updateUser(UIUser user) {
		return userDao.updateUser(user);
	}
	
	public RoutineResult deleteUser(long userId) {
		RoutineResult result = userDao.deleteUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public RoutineResult lockUser(long userId) {
		RoutineResult result = userDao.lockUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public RoutineResult unlockUser(long userId) {
		RoutineResult result = userDao.unlockUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public long cloneUser(long templateUserId, String newUsername,
			String newPassword) {
		UICloneUserRequest request = new UICloneUserRequest();
		request.setTemplateUserId(templateUserId);
		request.setUsername(newUsername);
		request.setPassword(newPassword);
		RoutineResult result = userDao.cloneUser(request);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
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

	public RoutineResult changeUserRoles(long userId, Collection<Long> rolesToAddIds,
			Collection<Long> rolesToRemoveIds,
			Collection<Long> rolesToGrantActionsIds) {
		rolesToGrantActionsIds = new HashSet<Long>(rolesToGrantActionsIds);
		rolesToGrantActionsIds.retainAll(rolesToAddIds);
		RoutineResult result = userDao.changeUserRoles(userId,
				StringUtils.collectionToCommaDelimitedString(rolesToAddIds),
				StringUtils.collectionToCommaDelimitedString(rolesToRemoveIds),
				StringUtils.collectionToCommaDelimitedString(rolesToGrantActionsIds));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
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

	public RoutineResult changeUserRoleActions(long userId,
			Collection<Long> roleActionToAddIds,
			Collection<Long> roleActionToRemoveIds) {
		RoutineResult result = userDao.changeUserRoleActions(userId,
						StringUtils.collectionToCommaDelimitedString(roleActionToAddIds),
						StringUtils.collectionToCommaDelimitedString(roleActionToRemoveIds));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public UIUserWithRolesAndActions getUserRoleActions(long userId,
			String subsystemIds, String actionNameSubstring,
			String roleNameSubstring) {
		return userDao.getUserRoleActions(userId, subsystemIds, actionNameSubstring, roleNameSubstring);
	}

}
