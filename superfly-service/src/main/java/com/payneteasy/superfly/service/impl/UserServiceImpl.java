package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.*;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Transactional
public class UserServiceImpl implements UserService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	private UserDao userDao;
	private NotificationService notificationService;
	private LoggerSink loggerSink;
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
    private IPolicyValidation<PasswordCheckContext> policyValidation;
    private SaltGenerator hotpSaltGenerator;
    private AccountPolicy accountPolicy;
    private HOTPService hotpService;
    private CreateUserStrategy createUserStrategy;
    private LockoutStrategy lockoutStrategy;


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

	@Required
	public void setHotpService(HOTPService hotpService) {
		this.hotpService = hotpService;
	}

    @Required
    public void setCreateUserStrategy(CreateUserStrategy createUserStrategy) {
        this.createUserStrategy = createUserStrategy;
    }

    @Required
    public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
        this.lockoutStrategy = lockoutStrategy;
    }

    public List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
			Long complectId, Long subsystemId, long startFrom, long recordsCount,
			int orderFieldNumber, boolean asc) {
		return userDao.getUsers(startFrom, recordsCount, orderFieldNumber,
				asc ? DaoConstants.ASC : DaoConstants.DESC, userNamePrefix,
				roleId, complectId, subsystemId);
	}

	public long getUsersCount(String userNamePrefix, Long roleId,
			Long complectId, Long subsystemId) {
		return userDao.getUsersCount(userNamePrefix, roleId, complectId,
				subsystemId);
	}

	public UserCreationResult createUser(UIUserForCreate user, String subsystemIdentifier) {
		UIUserForCreate userForDao = new UIUserForCreate();
		copyUserAndEncryptPassword(user, userForDao);
		userForDao.setHotpSalt(hotpSaltGenerator.generate());

		RoutineResult result = createUserStrategy.createUser(userForDao);

		loggerSink.info(logger, "CREATE_USER", result.isOk(), userForDao.getUsername());

        UserCreationResult userCreationResult = new UserCreationResult();
        userCreationResult.setResult(result);
		
		if (result.isOk()) {
            try {
                hotpService.sendTableIfSupported(
                        subsystemIdentifier,
                        userForDao.getId());
            } catch (MessageSendException e) {
                userCreationResult.setMailSendError(e.getMessage());
            }
        }
		
		return userCreationResult;
		// we're not notifying about this as user does not yet have any roles
		// or actions
	}

	private void copyUserAndEncryptPassword(UIUser user,
			UIUser userForDao) {
		BeanUtils.copyProperties(user, userForDao);
        userForDao.setSalt(saltSource.getSalt(user.getUsername()));
		userForDao.setPassword(passwordEncoder.encode(user.getPassword(),userForDao.getSalt()));
	}

	public UIUserDetails getUser(long userId) {
		return userDao.getUser(userId);
	}

	public RoutineResult updateUser(UIUser user) {
		UIUserForCreate userForDao = new UIUserForCreate();
		copyUserAndEncryptPassword(user, userForDao);
		// password and salt are not updated here
		RoutineResult result = userDao.updateUser(userForDao);
		loggerSink.info(logger, "UPDATE_USER", result.isOk(), user.getUsername());
		return result;
	}

	public RoutineResult deleteUser(long userId) {
		RoutineResult result = userDao.deleteUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "DELETE_USER", result.isOk(), String.valueOf(userId));
		return result;
	}

	public RoutineResult lockUser(long userId) {
		RoutineResult result = userDao.lockUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "LOCK_USER", result.isOk(), String.valueOf(userId));
		return result;
	}

	public String unlockUser(long userId, boolean unlockingSuspendedUser) {
		String newPassword = accountPolicy.unlockUser(userId, unlockingSuspendedUser);
		notificationService.notifyAboutUsersChanged();
		loggerSink.info(logger, "UNLOCK_USER", true, String.valueOf(userId));
		return newPassword;
	}

	public Long cloneUser(long templateUserId, String newUsername,
			String newPassword, String newEmail, String newPublicKey,
            String subsystemForEmailIdentifier) throws MessageSendException {
		UICloneUserRequest request = new UICloneUserRequest();
		request.setTemplateUserId(templateUserId);
		request.setUsername(newUsername);
		request.setEmail(newEmail);
        request.setSalt(saltSource.getSalt(newUsername));
        request.setHotpSalt(hotpSaltGenerator.generate());
		request.setPassword(passwordEncoder.encode(newPassword, request.getSalt()));
		request.setPublicKey(newPublicKey);

		RoutineResult result = createUserStrategy.cloneUser(request);

		if (result.isOk()) {
			hotpService.sendTableIfSupported(subsystemForEmailIdentifier, request.getId());
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CLONE_USER", result.isOk(), String.format("%s->%s", templateUserId, newUsername));
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
		return userDao.getAllUserRolesCount(userId, subsystemId == null ? null
				: String.valueOf(subsystemId));
	}

	public List<UIRoleForCheckbox> getUnmappedUserRoles(long userId,
			Long subsystemId, int startFrom, int recordsCount) {
		List<UIRoleForCheckbox> allRoles = userDao.getUnmappedUserRoles(
				startFrom, recordsCount, 4 /* role_id */, DaoConstants.ASC,
				userId, subsystemId == null ? null : String
						.valueOf(subsystemId));
		return allRoles;
	}

	public int getUnmappedUserRolesCount(long userId, Long subsystemId) {
		return userDao.getUnmappedUserRolesCount(userId,
				subsystemId == null ? null : String.valueOf(subsystemId));
	}

	public RoutineResult changeUserRoles(long userId,
			Collection<Long> rolesToAddIds, Collection<Long> rolesToRemoveIds,
			Collection<Long> rolesToGrantActionsIds) {
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
		return result;
	}

	public List<UIActionForCheckboxForUser> getAllUserActions(long userId,
			Long subsystemId, String actionSubstring, int startFrom,
			int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getAllUserActions(startFrom, recordsCount,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				userId, subsystemIds, actionSubstring);
	}

	public int getAllUserActionsCount(long userId, Long subsystemId,
			String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getAllUserActionsCount(userId, subsystemIds,
				actionSubstring);
	}

	public List<UIActionForCheckboxForUser> getUnmappedUserActions(long userId,
			Long subsystemId, long roleId, String actionSubstring, int startFrom,
			int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getUnmappedUserActions(startFrom, recordsCount,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				userId, subsystemIds, roleId, actionSubstring);
	}

	public int getUnmappedUserActionsCount(long userId, Long subsystemId,
			long roleId, String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getUnmappedUserActionsCount(userId, subsystemIds,
				roleId, actionSubstring);
	}

	public RoutineResult changeUserRoleActions(long userId,
			Collection<Long> roleActionToAddIds,
			Collection<Long> roleActionToRemoveIds) {
		RoutineResult result = userDao.changeUserRoleActions(
				userId,
				com.payneteasy.superfly.utils.StringUtils.collectionToCommaDelimitedString(roleActionToAddIds),
				com.payneteasy.superfly.utils.StringUtils.collectionToCommaDelimitedString(roleActionToRemoveIds));
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "CHANGE_USER_ROLE_ACTIONS", result.isOk(), String.valueOf(userId));
		return result;
	}

	public UIUserWithRolesAndActions getUserRoleActions(long userId,
			String subsystemIds, String actionNameSubstring,
			String roleNameSubstring) {
		return userDao.getUserRoleActions(userId, subsystemIds,
				actionNameSubstring, roleNameSubstring);
	}

	public RoutineResult addSubsystemWithRole(long userId, long roleId) {
		RoutineResult result = userDao.addSubsystemWithRole(userId, roleId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		return result;
	}

    public void validatePassword(String username,String password) throws PolicyValidationException {
        policyValidation.validate(new PasswordCheckContext(password, passwordEncoder, userDao.getUserPasswordHistoryAndCurrentPassword(username)));
    }

    public void expirePasswords(int days) {
    	accountPolicy.expirePasswordsIfNeeded(days, this);
    }

    public List<UIActionForCheckboxForUser> getMappedUserActions(long userId,
			Long subsystemId, long roleId, String actionSubstring, int startFrom,
			int recordsCount) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getMappedUserActions(startFrom, recordsCount,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				userId, subsystemIds, roleId, actionSubstring);
	}

	public int getMappedUserActionsCount(long userId, Long subsystemId,
			long roleId, String actionSubstring) {
		String subsystemIds = subsystemId == null ? null : subsystemId
				.toString();
		return userDao.getMappedUserActionsCount(userId, subsystemIds, roleId,
				actionSubstring);
	}

	public void suspendUser(long userId) {
		RoutineResult result = userDao.suspendUser(userId);
		if (result.isOk()) {
			notificationService.notifyAboutUsersChanged();
		}
		loggerSink.info(logger, "SUSPEND_USER", result.isOk(), String.valueOf(userId));
	}

	public void suspendUsers(int days) {
		accountPolicy.suspendUsersIfNeeded(days, this);
	}

    public void changeTempPassword(String userName, String password) {
        RoutineResult result = userDao.changeTempPassword(userName, passwordEncoder.encode(password, saltSource.getSalt(userName)));
        loggerSink.info(logger, "CHANGE_TEMP_PASSWORD", result.isOk(), userName);
    }

    @Override
    public UserLoginStatus getUserLoginStatus(String username, String password, String subsystemIdentifier) {
        String encodedPassword = passwordEncoder.encode(password, saltSource.getSalt(username));
        UserLoginStatus result = UserLoginStatus.findByDbStatus(
                userDao.getUserLoginStatus(username, encodedPassword, subsystemIdentifier));
        if (result == UserLoginStatus.FAILED) {
            lockoutStrategy.checkLoginsFailed(username, LockoutType.PASSWORD);
        }
        return result;
    }

}
