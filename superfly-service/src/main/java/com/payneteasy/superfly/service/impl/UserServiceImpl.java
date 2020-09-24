package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.User;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.UserWithActions;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.action.UIActionForCheckboxForUser;
import com.payneteasy.superfly.model.ui.role.UIRoleForCheckbox;
import com.payneteasy.superfly.model.ui.user.*;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    public void setPolicyValidation(IPolicyValidation<PasswordCheckContext> policyValidation) {
        this.policyValidation = policyValidation;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Autowired
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    @Autowired
    public void setHotpSaltGenerator(SaltGenerator hotpSaltGenerator) {
        this.hotpSaltGenerator = hotpSaltGenerator;
    }

    @Autowired
    public void setAccountPolicy(AccountPolicy accountPolicy) {
        this.accountPolicy = accountPolicy;
    }

    @Autowired
    public void setHotpService(HOTPService hotpService) {
        this.hotpService = hotpService;
    }

    @Autowired
    public void setCreateUserStrategy(CreateUserStrategy createUserStrategy) {
        this.createUserStrategy = createUserStrategy;
    }

    @Autowired
    public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
        this.lockoutStrategy = lockoutStrategy;
    }

    @Override
    public List<UIUserForList> getUsers(String userNamePrefix, Long roleId,
            Long complectId, Long subsystemId, long startFrom, long recordsCount,
            int orderFieldNumber, boolean asc) {
        return userDao.getUsers(startFrom, recordsCount, orderFieldNumber,
                asc ? DaoConstants.ASC : DaoConstants.DESC, userNamePrefix,
                roleId, complectId, subsystemId);
    }

    @Override
    public long getUsersCount(String userNamePrefix, Long roleId,
            Long complectId, Long subsystemId) {
        return userDao.getUsersCount(userNamePrefix, roleId, complectId,
                subsystemId);
    }

    @Override
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

    @Override
    public UIUserDetails getUser(long userId) {
        return userDao.getUser(userId);
    }

    @Override
    public RoutineResult updateUser(UIUser user) {
        UIUserForCreate userForDao = new UIUserForCreate();
        copyUserAndEncryptPassword(user, userForDao);
        // password and salt are not updated here
        RoutineResult result = userDao.updateUser(userForDao);
        loggerSink.info(logger, "UPDATE_USER", result.isOk(), user.getUsername());
        return result;
    }

    @Override
    public RoutineResult deleteUser(long userId) {
        RoutineResult result = userDao.deleteUser(userId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "DELETE_USER", result.isOk(), String.valueOf(userId));
        return result;
    }

    @Override
    public RoutineResult lockUser(long userId) {
        RoutineResult result = userDao.lockUser(userId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "LOCK_USER", result.isOk(), String.valueOf(userId));
        return result;
    }

    @Override
    public String unlockUser(long userId, boolean unlockingSuspendedUser) {
        String newPassword = accountPolicy.unlockUser(userId, unlockingSuspendedUser);
        notificationService.notifyAboutUsersChanged();
        loggerSink.info(logger, "UNLOCK_USER", true, String.valueOf(userId));
        return newPassword;
    }

    @Override
    public UserCloningResult cloneUser(long templateUserId, String newUsername,
            String newPassword, String newEmail, String newPublicKey,
            String subsystemForEmailIdentifier) {
        UICloneUserRequest request = new UICloneUserRequest();
        request.setTemplateUserId(templateUserId);
        request.setUsername(newUsername);
        request.setEmail(newEmail);
        request.setSalt(saltSource.getSalt(newUsername));
        request.setHotpSalt(hotpSaltGenerator.generate());
        request.setPassword(passwordEncoder.encode(newPassword, request.getSalt()));
        request.setPublicKey(newPublicKey);

        RoutineResult result = createUserStrategy.cloneUser(request);
        UserCloningResult userCloningResult = new UserCloningResult();
        if (request.getId() != null) {
            userCloningResult.setCloneId(request.getId());
        }

        if (result.isOk()) {
            try {
                hotpService.sendTableIfSupported(subsystemForEmailIdentifier, request.getId());
            } catch (MessageSendException e) {
                userCloningResult.setMailSendError(e.getMessage());
            }
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "CLONE_USER", result.isOk(), String.format("%s->%s", templateUserId, newUsername));
        return userCloningResult;
    }

    @Override
    public List<UIRoleForCheckbox> getAllUserRoles(long userId,
            Long subsystemId, int startFrom, int recordsCount) {
        List<UIRoleForCheckbox> allRoles = userDao.getAllUserRoles(startFrom,
                recordsCount, 4 /* role_id */, DaoConstants.ASC, userId,
                subsystemId == null ? null : String.valueOf(subsystemId));
        return allRoles;
    }

    @Override
    public int getAllUserRolesCount(long userId, Long subsystemId) {
        return userDao.getAllUserRolesCount(userId, subsystemId == null ? null
                : String.valueOf(subsystemId));
    }

    @Override
    public List<UIRoleForCheckbox> getUnmappedUserRoles(long userId,
            Long subsystemId, int startFrom, int recordsCount) {
        List<UIRoleForCheckbox> allRoles = userDao.getUnmappedUserRoles(
                startFrom, recordsCount, 4 /* role_id */, DaoConstants.ASC,
                userId, subsystemId == null ? null : String
                        .valueOf(subsystemId));
        return allRoles;
    }

    @Override
    public int getUnmappedUserRolesCount(long userId, Long subsystemId) {
        return userDao.getUnmappedUserRolesCount(userId,
                subsystemId == null ? null : String.valueOf(subsystemId));
    }

    @Override
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

    @Override
    public List<UIActionForCheckboxForUser> getAllUserActions(long userId,
            Long subsystemId, String actionSubstring, int startFrom,
            int recordsCount) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getAllUserActions(startFrom, recordsCount,
                DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
                userId, subsystemIds, actionSubstring);
    }

    @Override
    public int getAllUserActionsCount(long userId, Long subsystemId,
            String actionSubstring) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getAllUserActionsCount(userId, subsystemIds,
                actionSubstring);
    }

    @Override
    public List<UIActionForCheckboxForUser> getUnmappedUserActions(long userId,
            Long subsystemId, long roleId, String actionSubstring, int startFrom,
            int recordsCount) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getUnmappedUserActions(startFrom, recordsCount,
                DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
                userId, subsystemIds, roleId, actionSubstring);
    }

    @Override
    public int getUnmappedUserActionsCount(long userId, Long subsystemId,
            long roleId, String actionSubstring) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getUnmappedUserActionsCount(userId, subsystemIds,
                roleId, actionSubstring);
    }

    @Override
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

    @Override
    public UIUserWithRolesAndActions getUserRoleActions(long userId,
            String subsystemIds, String actionNameSubstring,
            String roleNameSubstring) {
        return userDao.getUserRoleActions(userId, subsystemIds,
                actionNameSubstring, roleNameSubstring);
    }

    @Override
    public RoutineResult addSubsystemWithRole(long userId, long roleId) {
        RoutineResult result = userDao.addSubsystemWithRole(userId, roleId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        return result;
    }

    @Override
    public void validatePassword(String username,String password) throws PolicyValidationException {
        policyValidation.validate(new PasswordCheckContext(password, passwordEncoder, userDao.getUserPasswordHistoryAndCurrentPassword(username)));
    }

    @Override
    public void expirePasswords(int days) {
        accountPolicy.expirePasswordsIfNeeded(days, this);
    }

    @Override
    public List<UIActionForCheckboxForUser> getMappedUserActions(long userId,
            Long subsystemId, long roleId, String actionSubstring, int startFrom,
            int recordsCount) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getMappedUserActions(startFrom, recordsCount,
                DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
                userId, subsystemIds, roleId, actionSubstring);
    }

    @Override
    public int getMappedUserActionsCount(long userId, Long subsystemId,
            long roleId, String actionSubstring) {
        String subsystemIds = subsystemId == null ? null : subsystemId
                .toString();
        return userDao.getMappedUserActionsCount(userId, subsystemIds, roleId,
                actionSubstring);
    }

    @Override
    public void suspendUser(long userId) {
        RoutineResult result = userDao.suspendUser(userId);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "SUSPEND_USER", result.isOk(), String.valueOf(userId));
    }

    @Override
    public void suspendUsers(int days) {
        accountPolicy.suspendUsersIfNeeded(days, this);
    }

    @Override
    public void changeTempPassword(String userName, String password) {
        RoutineResult result = userDao.changeTempPassword(userName, passwordEncoder.encode(password, saltSource.getSalt(userName)));
        loggerSink.info(logger, "CHANGE_TEMP_PASSWORD", result.isOk(), userName);
    }

    @Override
    public UserLoginStatus checkUserCanLoginWithThisPassword(String username, String password, String subsystemIdentifier) {
        String encodedPassword = passwordEncoder.encode(password, saltSource.getSalt(username));
        UserLoginStatus result = UserLoginStatus.findByDbStatus(
                userDao.getUserLoginStatus(username, encodedPassword, subsystemIdentifier));
        if (result == UserLoginStatus.FAILED) {
            lockoutStrategy.checkLoginsFailed(username, LockoutType.PASSWORD);
        }
        return result;
    }

    @Override
    public List<User> getUsersWithExpiredPasswords(int days) {
        return userDao.getUsersWithExpiredPasswords(days);
    }

    @Override
    public List<User> getUsersToSuspend(int days) {
        return userDao.getUsersToSuspend(days);
    }

    @Override
    public RoutineResult unlockUser(long userId) {
        return userDao.unlockUser(userId);
    }

    @Override
    public RoutineResult unlockSuspendedUser(long userId, String newPassword) {
        return userDao.unlockSuspendedUser(userId,newPassword);
    }

    @Override
    public RoutineResult cloneUser(UICloneUserRequest cloneUserRequest) {
        return userDao.cloneUser(cloneUserRequest);
    }

    @Override
    public RoutineResult registerUser(UserRegisterRequest registerUser) {
        return userDao.registerUser(registerUser);
    }

    @Override
    public RoutineResult lockoutConditionnally(String userName, long maxLoginsFailed, String lockoutType) {
        return userDao.lockoutConditionnally(userName,maxLoginsFailed,lockoutType);
    }

    @Override
    public void persistGoogleAuthMasterKeyForUsername(String username, String masterKey) {
        userDao.persistGoogleAuthMasterKeyForUsername(username,masterKey);
    }

    @Override
    public String getGoogleAuthMasterKeyByUsername(String username) {
        return userDao.getGoogleAuthMasterKeyByUsername(username);
    }

    @Override
    public String getUserSalt(String userName) {
        return userDao.getUserSalt(userName);
    }

    @Override
    public RoutineResult createUser(UIUserForCreate user) {
        return userDao.createUser(user);
    }

    @Override
    public RoutineResult resetPassword(long userId, String password) {
        return userDao.resetPassword(userId,password);
    }

    @Override
    public void updateUserSalt(String username, String salt) {
        userDao.updateUserSalt(username,salt);
    }

    @Override
    public String getUserSaltByUserId(long userId) {
        return userDao.getUserSaltByUserId(userId);
    }

    @Override
    public void updateUserSaltByUserId(long userId, String salt) {
        userDao.updateUserSaltByUserId(userId,salt);
    }

    @Override
    public AuthSession authenticate(String username, String password, String subsystemName, String ipAddress, String sessionInfo) {
        return userDao.authenticate(username,password,subsystemName,ipAddress,sessionInfo);
    }

    @Override
    public AuthSession pseudoAuthenticate(String username, String subsystemName) {
        return userDao.pseudoAuthenticate(username,subsystemName);
    }

    @Override
    public RoutineResult changeUserRole(String username, String newRole, String subsystemName) {
        return userDao.changeUserRole(username,newRole,subsystemName);
    }

    @Override
    public void completeUser(String username) {
        userDao.completeUser(username);
    }

    @Override
    public List<UserWithActions> getUsersAndActions(String subsystemName) {
        return userDao.getUsersAndActions(subsystemName);
    }

    @Override
    public List<PasswordSaltPair> getUserPasswordHistoryAndCurrentPassword(String username) {
        return userDao.getUserPasswordHistoryAndCurrentPassword(username);
    }

    @Override
    public RoutineResult grantRolesToUser(long userId, String subsystemName, String principalNames) {
        return userDao.grantRolesToUser(userId,subsystemName,principalNames);
    }

    @Override
    public AuthSession exchangeSubsystemToken(String subsystemToken) {
        return userDao.exchangeSubsystemToken(subsystemToken);
    }

    @Override
    public List<UserWithStatus> getUserStatuses(String userNames) {
        return userDao.getUserStatuses(userNames);
    }

    @Override
    public UserForDescription getUserForDescription(String username) {
        return userDao.getUserForDescription(username);
    }

    @Override
    public void updateUserForDescription(UserForDescription user) {
        userDao.updateUserForDescription(user);
    }

    @Override
    public void incrementHOTPLoginsFailed(String username) {
        userDao.incrementHOTPLoginsFailed(username);
    }

    @Override
    public void clearHOTPLoginsFailed(String username) {
        userDao.clearHOTPLoginsFailed(username);
    }


    @Override
    public void updateUserOtpType(String username, String otpType) {
        userDao.updateUserOtpType(username,otpType);
    }

    @Override
    public void updateUserIsOtpOptionalValue(String username, boolean isOtpOptional) {
        userDao.updateUserIsOtpOptionalValue(username,isOtpOptional);
    }
}
