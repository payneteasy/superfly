package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.common.utils.UserNames;
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
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.IPolicyValidation;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.UserInfoService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String IMPOSSIBLE_USER_SALT = "impossible-user";

    // error_message of login_locked when the call actually locked the account
    private static final String LOCKED_MARKER = "ACCOUNT_LOCKED";

    private UserDao userDao;
    private NotificationService notificationService;
    private LoggerSink loggerSink;
    private PasswordEncoder passwordEncoder;
    private PasswordEncoder legacyPasswordEncoder;
    private SaltSource saltSource;
    private IPolicyValidation<PasswordCheckContext> policyValidation;
    private SaltGenerator hotpSaltGenerator;
    private AccountPolicy accountPolicy;
    private HOTPService hotpService;
    private CreateUserStrategy createUserStrategy;
    private LockoutStrategy lockoutStrategy;
    private UserService self;
    private UserInfoService userInfoService;


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
    public void setUserInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    @Qualifier("messageDigestPasswordEncoder")
    public void setLegacyPasswordEncoder(PasswordEncoder legacyPasswordEncoder) {
        this.legacyPasswordEncoder = legacyPasswordEncoder;
    }

    @Autowired
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    @Autowired
    public void setHotpSaltGenerator(SaltGenerator hotpSaltGenerator) {
        this.hotpSaltGenerator = hotpSaltGenerator;
    }

    // the transactional proxy of this bean: calls through `this` would bypass it
    @Autowired
    @Lazy
    public void setSelf(UserService self) {
        this.self = self;
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

        return userCreationResult;
        // we're not notifying about this as user does not yet have any roles
        // or actions
    }

    private void copyUserAndEncryptPassword(UIUser user,
            UIUser userForDao) {
        BeanUtils.copyProperties(user, userForDao);
        userForDao.setSalt(saltSource.getSalt(user.getUsername()));
        // null means "password is not changed" (e.g. updateUser from the admin UI)
        if (user.getPassword() != null) {
            userForDao.setPassword(passwordEncoder.encode(user.getPassword(),userForDao.getSalt()));
        }
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
        String added = StringUtils.collectionToCommaDelimitedString(rolesToAddIds);
        String removed = StringUtils.collectionToCommaDelimitedString(rolesToRemoveIds);
        String grantActions = StringUtils.collectionToCommaDelimitedString(rolesToGrantActionsIds);
        RoutineResult result = userDao.changeUserRoles(userId, added, removed, grantActions);
        if (result.isOk()) {
            notificationService.notifyAboutUsersChanged();
        }
        loggerSink.info(logger, "CHANGE_USER_ROLES", result.isOk(), String.valueOf(userId),
                "added=" + added + ", removed=" + removed + ", grantActions=" + grantActions);
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
        policyValidation.validate(new PasswordCheckContext(password, legacyPasswordEncoder, userDao.getUserPasswordHistoryAndCurrentPassword(username)));
    }

    // no transaction around the loop: each user is processed in its own transaction through the proxy,
    // so the event of one user is committed at once, not at the end of the whole batch
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void expirePasswords(int days) {
        accountPolicy.expirePasswordsIfNeeded(days, self);
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

    // see expirePasswords
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void suspendUsers(int days) {
        accountPolicy.suspendUsersIfNeeded(days, self);
    }

    @Override
    public void changeTempPassword(String userName, String password) {
        RoutineResult result = userDao.changeTempPassword(userName, passwordEncoder.encode(password, saltSource.getSalt(userName)));
        loggerSink.info(logger, "CHANGE_TEMP_PASSWORD", result.isOk(), userName);
    }

    @Override
    public UserLoginStatus checkUserCanLoginWithThisPassword(String username, String password, String subsystemIdentifier) {
        boolean possible = UserNames.isPossible(username);
        // a name that cannot exist must not reach the database even for the salt
        String salt = possible ? saltSource.getSalt(username) : IMPOSSIBLE_USER_SALT;
        if (!possible || !isUserAccessibleFrom(username, subsystemIdentifier)) {
            // same hashing cost as a regular attempt, but the failed-login counter is not touched
            if (password != null) {
                passwordEncoder.encode(password, salt);
                legacyPasswordEncoder.encode(password, salt);
            }
            logger.warn("Subsystem {} was denied SSO password login on user {}", sanitize(subsystemIdentifier), sanitize(username));
            loggerSink.info(logger, "SSO_PASSWORD_LOGIN", false, username, "subsystem=" + subsystemIdentifier);
            return UserLoginStatus.FAILED;
        }
        // null password is an ordinary failed attempt, not an exception
        UserLoginStatus result = UserLoginStatus.findByDbStatus(
                userDao.getUserLoginStatus(username,
                        password == null ? Pbkdf2PasswordEncoder.NEVER_MATCHING_HASH : passwordEncoder.encode(password, salt),
                        password == null ? null : legacyPasswordEncoder.encode(password, salt), subsystemIdentifier,
                        remoteAddress()));
        loggerSink.info(logger, "SSO_PASSWORD_LOGIN", result != UserLoginStatus.FAILED, username,
                "subsystem=" + subsystemIdentifier + (result == UserLoginStatus.TEMP_PASSWORD ? ", tempPassword=true" : ""));
        if (result == UserLoginStatus.FAILED) {
            lockoutStrategy.checkLoginsFailed(username, LockoutType.PASSWORD);
        }
        return result;
    }

    @Override
    public boolean isUserAccessibleFrom(String username, String subsystemIdentifier) {
        if (username == null || subsystemIdentifier == null) {
            return false;
        }
        return userHasRolesInSubsystem(username, subsystemIdentifier);
    }

    @Override
    public boolean isUserManageableFrom(String username, String subsystemIdentifier) {
        return isUserAccessibleFrom(username, subsystemIdentifier)
                && !userHasRolesInSubsystem(username, LocalSecurityServiceImpl.DEFAULT_LOCAL_SUBSYSTEM_NAME);
    }

    // values come from the request: strip line breaks to prevent log injection
    private static String sanitize(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n\\t]", "_");
    }

    // request threads only: scheduled jobs have no client
    private String remoteAddress() {
        return userInfoService == null ? null : userInfoService.getRemoteAddress();
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
        RoutineResult result = userDao.unlockSuspendedUser(userId,newPassword);
        loggerSink.info(logger, "UNLOCK_SUSPENDED_USER", result.isOk(), String.valueOf(userId));
        return result;
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
        RoutineResult result = userDao.lockoutConditionnally(userName,maxLoginsFailed,lockoutType);
        if (LOCKED_MARKER.equals(result.getErrorMessage())) {
            // the caller is the failed login itself, so the actor is the system
            loggerSink.info(logger, "AUTO_LOCK_USER", true, userName, "reason=" + lockoutType + ", maxLoginsFailed=" + maxLoginsFailed);
        }
        return result;
    }

    @Override
    public void persistOtpMasterKeyForUsername(String username, String masterKey) {
        userDao.persistGoogleAuthMasterKeyForUsername(username,masterKey);
        loggerSink.info(logger, "PERSIST_OTP_MASTER_KEY", true, username);
    }

    @Override
    public void persistOtpMasterKeyIfUnchanged(String username, String oldMasterKey, String newMasterKey) {
        userDao.persistGoogleAuthMasterKeyIfUnchanged(username, oldMasterKey, newMasterKey);
    }

    @Override
    public boolean markOtpStepUsed(String username, long step) {
        return userDao.saveOtpLastUsedStep(username, step) > 0;
    }

    @Override
    public boolean userHasRolesInSubsystem(String username, String subsystemName) {
        return "Y".equals(userDao.userHasRolesInSubsystem(username, subsystemName));
    }

    @Override
    public String getOtpMasterKeyByUsername(String username) {
        return userDao.getGoogleAuthMasterKeyByUsername(username);
    }

    @Override
    public void persistOtpPendingMasterKey(String username, String pendingMasterKey) {
        userDao.persistOtpPendingMasterKey(username, pendingMasterKey);
        loggerSink.info(logger, "PERSIST_OTP_PENDING_MASTER_KEY", true, username);
    }

    @Override
    public String getOtpPendingMasterKeyByUsername(String username) {
        return userDao.getOtpPendingMasterKeyByUsername(username);
    }

    @Override
    public boolean confirmOtpPendingMasterKey(String username, String pendingMasterKey, long step) {
        boolean confirmed = userDao.confirmOtpPendingMasterKey(username, pendingMasterKey, step) > 0;
        loggerSink.info(logger, "CONFIRM_OTP_MASTER_KEY", confirmed, username);
        return confirmed;
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
    public AuthSession authenticate(String username, String password, String legacyPassword, String subsystemName, String ipAddress, String sessionInfo) {
        return userDao.authenticate(username,password,legacyPassword,subsystemName,ipAddress,sessionInfo);
    }

    @Override
    public AuthSession pseudoAuthenticate(String username, String subsystemName) {
        return userDao.pseudoAuthenticate(username,subsystemName);
    }

    @Override
    public RoutineResult changeUserRole(String username, String newRole, String subsystemName) {
        RoutineResult result = userDao.changeUserRole(username,newRole,subsystemName);
        loggerSink.info(logger, "REMOTE_CHANGE_USER_ROLE", result.isOk(), username,
                "role=" + newRole + ", subsystem=" + subsystemName);
        return result;
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
    public AuthSession exchangeSubsystemToken(String subsystemToken, String callerSubsystem) {
        return userDao.exchangeSubsystemToken(subsystemToken, callerSubsystem);
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
        loggerSink.info(logger, "UPDATE_USER_DESCRIPTION", true, user.getUsername());
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
        loggerSink.info(logger, "CHANGE_USER_OTP_TYPE", true, username, "otpType=" + otpType);
    }

    @Override
    public void updateUserIsOtpOptionalValue(String username, boolean isOtpOptional) {
        userDao.updateUserIsOtpOptionalValue(username,isOtpOptional);
        loggerSink.info(logger, "CHANGE_USER_OTP_OPTIONAL", true, username, "otpOptional=" + isOtpOptional);
    }

    @Override
    public void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException {
        hotpService.persistOtpKey(otpType, username, key);
    }

    @Override
    public boolean authenticateUsingOTP(String username, String otp) {
        UserForDescription userForDescription = getUserForDescription(username);
        if (userForDescription == null) {
            return false;
        }
        boolean ok = false;
        switch (userForDescription.getOtpType()) {
            case GOOGLE_AUTH:
                try {
                    ok = hotpService.validateGoogleTimePassword(username, otp) == CheckOtpResult.Status.SUCCESS;
                } catch (SsoDecryptException e) {
                    logger.warn("Can't decrypt secret key for {}", username);
                }
                break;
            case NONE:
            default:
                ok = true;
        }

        if (!ok) {
            logger.warn("OTP check failed {}", username);
            incrementHOTPLoginsFailed(username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.HOTP);
        } else {
            clearHOTPLoginsFailed(username);
        }

        loggerSink.info(logger, "REMOTE_OTP_CHECK", ok, username);
        return ok;
    }


}
