package com.payneteasy.superfly.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.UICloneUserRequest;
import com.payneteasy.superfly.model.ui.user.UIUser;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import com.payneteasy.superfly.model.ui.user.UIUserForList;
import com.payneteasy.superfly.model.ui.user.UIUserWithRolesAndActions;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltGenerator;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.SyslogService;
import com.payneteasy.superfly.service.UserService;

@Transactional
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private UserDao userDao;
	private NotificationService notificationService;
	private LoggerSink loggerSink;
	private SyslogService syslogService;
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
	private IPolicyValidation<PasswordCheckContext> policyValidation;
	private SaltGenerator hotpSaltGenerator;
	private AccountPolicy accountPolicy;

	@Required
	public void setPolicyValidation(IPolicyValidation<PasswordCheckContext> policyValidation) {
		this.policyValidation = policyValidation;
	}

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Required
	public void setLoggerSink(LoggerSink loggerSink) {
		this.loggerSink = loggerSink;
	}

	@Required
	public void setSyslogService(SyslogService syslogService) {
		this.syslogService = syslogService;
	}

	@Required
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Required
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	@Required
	public void setHotpSaltGenerator(SaltGenerator hotpSaltGenerator) {
		this.hotpSaltGenerator = hotpSaltGenerator;
	}

	@Required
	public void setAccountPolicy(AccountPolicy accountPolicy) {
		this.accountPolicy = accountPolicy;
	}

	public List<UIUserForList> getUsers(String userNamePrefix, Long roleId, Long complectId, Long subsystemId,
			int startFrom, int recordsCount, int orderFieldNumber, boolean asc) {
		return userDao.getUsers(startFrom, recordsCount, orderFieldNumber, asc ? DaoConstants.ASC : DaoConstants.DESC,
				userNamePrefix, roleId, complectId, subsystemId);
	}

	public int getUsersCount(String userNamePrefix, Long roleId, Long complectId, Long subsystemId) {
		return userDao.getUsersCount(userNamePrefix, roleId, complectId, subsystemId);
	}

	public RoutineResult createUser(UIUserForCreate user) {
		UIUserForCreate userForDao = new UIUserForCreate();
		copyUserAndEncryptPassword(user, userForDao);
		userForDao.setHotpSalt(hotpSaltGenerator.generate());
		RoutineResult result = userDao.createUser(userForDao);
		loggerSink.info(logger, "CREATE_USER", result.isOk(), user.getUsername());
		syslogService.sendLogMessage("CREATE_USER", result.isOk(), user.getUsername());
		return result;
		// we're not notifying about this as user does not yet have any roles
		// or actions
	}

	private void copyUserAndEncryptPassword(UIUser user, UIUser userForDao) {
		BeanUtils.copyProperties(user, userForDao);
		userForDao.setSalt(saltSource.getSalt(user.getUsername()));
		userForDao.setPassword(passwordEncoder.encode(user.getPassword(), userForDao.getSalt()));
	}

	public UIUser getUser(long userId) {
		return userDao.getUser(userId);
	}

	public RoutineResult updateUser(UIUser user) {
		UIUserForCreate userForDao = new UIUserForCreate();
		copyUserAndEncryptPassword(user, userForDao);
		RoutineResult result = userDao.updateUser(userForDao);
		loggerSink.info(logger, "UPDATE_USER", result.isOk(), user.getUsername());
		syslogService.sendLogMessage("UPDATE_USER", result.isOk(), user.getUsername());
		return result;
	}

	public RoutineResult deleteUser(long userId) {
		RoutineResult result = userDao.deleteUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "DELETE_USER", result.isOk(), String.valueOf(userId));
		syslogService.sendLogMessage("DELETE_USER", result.isOk(),String.valueOf(userId));
		return result;
	}

	public RoutineResult lockUser(long userId) {
		RoutineResult result = userDao.lockUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "LOCK_USER", result.isOk(), String.valueOf(userId));
		syslogService.sendLogMessage("LOCK_USER", result.isOk(),String.valueOf(userId));
		return result;
	}

	public String unlockUser(long userId, boolean unlockingSuspendedUser) {
		String newPassword = accountPolicy.unlockUser(userId, unlockingSuspendedUser);
		notificationService.notifyAboutUsersChanged();
		loggerSink.info(logger, "UNLOCK_USER", true, String.valueOf(userId));
		syslogService.sendLogMessage("UNLOCK_USER", true,String.valueOf(userId));
		return newPassword;
	}

	public Long cloneUser(long templateUserId, String newUsername, String newPassword, String newEmail) {
		UICloneUserRequest request = new UICloneUserRequest();
		request.setTemplateUserId(templateUserId);
		request.setUsername(newUsername);
		request.setEmail(newEmail);
		request.setSalt(saltSource.getSalt(newUsername));
		request.setHotpSalt(hotpSaltGenerator.generate());
		request.setPassword(passwordEncoder.encode(newPassword, request.getSalt()));
		RoutineResult result = userDao.cloneUser(request);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CLONE_USER", result.isOk(), String.format("%s->%s", templateUserId, newUsername));
		syslogService.sendLogMessage("CLONE_USER", result.isOk(), String.format("%s->%s", templateUserId, newUsername));
		return request.getId();
	}

	public List<UIRoleForCheckbox> getAllUserRoles(long userId, Long subsystemId, int startFrom, int recordsCount) {
		List<UIRoleForCheckbox> allRoles = userDao.getAllUserRoles(startFrom, recordsCount, 4 /* role_id */,
				DaoConstants.ASC, userId, subsystemId == null ? null : String.valueOf(subsystemId));
		return allRoles;
	}

	public int getAllUserRolesCount(long userId, Long subsystemId) {
		return userDao.getAllUserRolesCount(userId, subsystemId == null ? null : String.valueOf(subsystemId));
	}

	public List<UIRoleForCheckbox> getUnmappedUserRoles(long userId, Long subsystemId, int startFrom, int recordsCount) {
		List<UIRoleForCheckbox> allRoles = userDao.getUnmappedUserRoles(startFrom, recordsCount, 4 /* role_id */,
				DaoConstants.ASC, userId, subsystemId == null ? null : String.valueOf(subsystemId));
		return allRoles;
	}

	public int getUnmappedUserRolesCount(long userId, Long subsystemId) {
		return userDao.getUnmappedUserRolesCount(userId, subsystemId == null ? null : String.valueOf(subsystemId));
	}

	public RoutineResult changeUserRoles(long userId, Collection<Long> rolesToAddIds,
			Collection<Long> rolesToRemoveIds, Collection<Long> rolesToGrantActionsIds) {
		if (rolesToGrantActionsIds == null) {

		} else {
			rolesToGrantActionsIds = new HashSet<Long>(rolesToGrantActionsIds);
			rolesToGrantActionsIds.retainAll(rolesToAddIds);
		}
		RoutineResult result = userDao.changeUserRoles(userId,
				StringUtils.collectionToCommaDelimitedString(rolesToAddIds),
				StringUtils.collectionToCommaDelimitedString(rolesToRemoveIds),
				StringUtils.collectionToCommaDelimitedString(rolesToGrantActionsIds));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CHANGE_USER_ROLES", result.isOk(), String.valueOf(userId));
		syslogService.sendLogMessage("CHANGE_USER_ROLES", result.isOk(), String.valueOf(userId));
		return result;
	}

	public List<UIActionForCheckboxForUser> getAllUserActions(long userId, Long subsystemId, String actionSubstring,
			int startFrom, int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getAllUserActions(startFrom, recordsCount, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				DaoConstants.ASC, userId, subsystemIds, actionSubstring);
	}

	public int getAllUserActionsCount(long userId, Long subsystemId, String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getAllUserActionsCount(userId, subsystemIds, actionSubstring);
	}

	public List<UIActionForCheckboxForUser> getUnmappedUserActions(long userId, Long subsystemId,
			String actionSubstring, int startFrom, int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getUnmappedUserActions(startFrom, recordsCount, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				DaoConstants.ASC, userId, subsystemIds, actionSubstring);
	}

	public int getUnmappedUserActionsCount(long userId, Long subsystemId, String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getUnmappedUserActionsCount(userId, subsystemIds, actionSubstring);
	}

	public RoutineResult changeUserRoleActions(long userId, Collection<Long> roleActionToAddIds,
			Collection<Long> roleActionToRemoveIds) {
		RoutineResult result = userDao.changeUserRoleActions(userId,
				com.payneteasy.superfly.utils.StringUtils.collectionToCommaDelimitedString(roleActionToAddIds),
				com.payneteasy.superfly.utils.StringUtils.collectionToCommaDelimitedString(roleActionToRemoveIds));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CHANGE_USER_ROLE_ACTIONS", result.isOk(), String.valueOf(userId));
		syslogService.sendLogMessage("CHANGE_USER_ROLE_ACTIONS", result.isOk(), String.valueOf(userId));
		return result;
	}

	public UIUserWithRolesAndActions getUserRoleActions(long userId, String subsystemIds, String actionNameSubstring,
			String roleNameSubstring) {
		return userDao.getUserRoleActions(userId, subsystemIds, actionNameSubstring, roleNameSubstring);
	}

	public RoutineResult addSubsystemWithRole(long userId, long roleId) {
		RoutineResult result = userDao.addSubsystemWithRole(userId, roleId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

	public void validatePassword(String username, String password) throws PolicyValidationException {
		policyValidation.validate(new PasswordCheckContext(password, passwordEncoder, userDao
				.getUserPasswordHistory(username)));
	}

	public void expirePasswords(int days) {
		accountPolicy.expirePasswordsIfNeeded(days, this);
	}

	public List<UIActionForCheckboxForUser> getMappedUserActions(long userId, Long subsystemId, String actionSubstring,
			int startFrom, int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getMappedUserActions(startFrom, recordsCount, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				DaoConstants.ASC, userId, subsystemIds, actionSubstring);
	}

	public int getMappedUserActionsCount(long userId, Long subsystemId, String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId.toString();
		return userDao.getMappedUserActionsCount(userId, subsystemIds, actionSubstring);
	}

	public void suspendUser(long userId) {
		RoutineResult result = userDao.suspendUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "SUSPEND_USER", result.isOk(), String.valueOf(userId));
		syslogService.sendLogMessage("SUSPEND_USER", result.isOk(), String.valueOf(userId));
	}

	public void suspendUsers(int days) {
		accountPolicy.suspendUsersIfNeeded(days, this);
	}

	public void changeTempPassword(String userName, String password) {
		userDao.changeTempPassword(userName, passwordEncoder.encode(password, saltSource.getSalt(userName)));
	}

}
