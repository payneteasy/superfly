package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.*;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.*;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import com.payneteasy.superfly.utils.PGPKeyValidator;
import com.payneteasy.superfly.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class InternalSSOServiceImpl implements InternalSSOService {

    private static final Logger logger = LoggerFactory.getLogger(InternalSSOServiceImpl.class);

    private       UserService          userService;
    private       ActionService        actionService;
    private       SessionService       sessionService;
    private       NotificationService  notificationService;
    private       LoggerSink           loggerSink;
    private       PasswordEncoder      passwordEncoder;
    private       SaltSource           saltSource;
    private       SaltGenerator        hotpSaltGenerator;
    private       LockoutStrategy      lockoutStrategy;
    private       RegisterUserStrategy registerUserStrategy;
    private       PublicKeyCrypto      publicKeyCrypto;
    private       HOTPService          hotpService;
    private final Set<String>          notSavedActions = Collections.singleton("action_temp_password");

    private AbstractPolicyValidation<PasswordCheckContext> policyValidation;

    @Autowired
    public void setPolicyValidation(AbstractPolicyValidation<PasswordCheckContext> policyValidation) {
        this.policyValidation = policyValidation;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Autowired
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
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
    public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
        this.lockoutStrategy = lockoutStrategy;
    }

    @Autowired
    public void setRegisterUserStrategy(RegisterUserStrategy registerUserStrategy) {
        this.registerUserStrategy = registerUserStrategy;
    }

    @Autowired
    public void setPublicKeyCrypto(PublicKeyCrypto publicKeyCrypto) {
        this.publicKeyCrypto = publicKeyCrypto;
    }

    @Autowired
    public void setHotpService(HOTPService hotpService) {
        this.hotpService = hotpService;
    }

    @Override
    public SSOUser authenticate(String username, String password, String subsystemIdentifier, String userIpAddress,
                                String sessionInfo) {
        SSOUser ssoUser;
        String  encPassword = passwordEncoder.encode(password, saltSource.getSalt(username));
        AuthSession session = userService.authenticate(username, encPassword,
                subsystemIdentifier, userIpAddress, sessionInfo);
        boolean ok = session != null && session.getSessionId() != null;
        loggerSink.info(logger, "REMOTE_LOGIN", ok, username);
        if (ok) {
            ssoUser = buildSSOUser(session);
        } else {
            logger.warn("No roles for user {}", username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.PASSWORD);
            ssoUser = null;
        }
        return ssoUser;
    }

    public boolean checkOtp(SSOUser user, String code) {
        if (user.isOtpOptional() && (code == null || code.trim().isEmpty())) {
            return true;
        }
        return authenticateByOtpType(user.getOtpType(), user.getName(), code);
    }

    @Override
    public SSOUser pseudoAuthenticate(String username, String subsystemIdentifier) {
        SSOUser     ssoUser;
        AuthSession session = userService.pseudoAuthenticate(username, subsystemIdentifier);
        boolean     ok      = session != null && session.getSessionId() != null;
        loggerSink.info(logger, "REMOTE_PSEUDO_LOGIN", ok, username);
        if (ok) {
            ssoUser = buildSSOUser(session);
        } else {
            logger.warn("No roles for user '{}' during pseudo-login", username);
            ssoUser = null;
        }
        return ssoUser;
    }

    private SSOUser buildSSOUser(AuthSession session) {
        SSOUser        ssoUser;
        List<AuthRole> authRoles = session.getRoles();
        if (authRoles.size() == 1 && authRoles.getFirst().getRoleName() == null) {
            // actually it's empty
            authRoles = Collections.emptyList();
        }

        Map<SSORole, SSOAction[]> actionsMap = new HashMap<>(authRoles.size());
        for (AuthRole authRole : authRoles) {
            SSORole     ssoRole = new SSORole(authRole.getRoleName());
            SSOAction[] actions = convertToSSOActions(authRole.getActions());
            actionsMap.put(ssoRole, actions);
        }
        ssoUser = new SSOUser(session.getUsername(), actionsMap, Collections.emptyMap());
        ssoUser.setSessionId(String.valueOf(session.getSessionId()));
        ssoUser.setOtpType(session.otpType());
        ssoUser.setOtpOptional(session.isOtpOptional());
        return ssoUser;
    }

    protected SSOAction[] convertToSSOActions(List<AuthAction> authActions) {
        SSOAction[] actions = new SSOAction[authActions.size()];
        for (int i = 0; i < authActions.size(); i++) {
            AuthAction authAction = authActions.get(i);
            SSOAction  ssoAction  = new SSOAction(authAction.getActionName(), authAction.isLogAction());
            actions[i] = ssoAction;
        }
        return actions;
    }

    public void saveSystemData(String subsystemIdentifier, ActionDescription[] actionDescriptions) {
        List<ActionToSave> actions = convertActionDescriptions(actionDescriptions);
        actionService.saveActions(subsystemIdentifier, actions);
        if (logger.isDebugEnabled()) {
            logger.debug("Saved actions for subsystem " + subsystemIdentifier + ": " + actions.size());
            logger.debug("Actions are: " + Arrays.asList(actionDescriptions));
        }
    }

    private List<ActionToSave> convertActionDescriptions(ActionDescription[] actionDescriptions) {
        List<ActionToSave> actions = new ArrayList<>(actionDescriptions.length);
        for (ActionDescription description : actionDescriptions) {
            if (!notSavedActions.contains(description.getName().toLowerCase())) {
                ActionToSave action = new ActionToSave();
                action.setName(description.getName());
                action.setDescription(description.getDescription());
                actions.add(action);
            }
        }
        return actions;
    }

    public List<SSOUserWithActions> getUsersWithActions(String subsystemIdentifier) {
        List<UserWithActions>    users  = userService.getUsersAndActions(subsystemIdentifier);
        List<SSOUserWithActions> result = new ArrayList<>(users.size());
        for (UserWithActions user : users) {
            result.add(convertToSSOUser(user));
        }
        return result;
    }

    public void registerUser(String username, String password, String email, String subsystemIdentifier,
                             RoleGrantSpecification[] roleGrants, String name, String surname, String secretQuestion,
                             String secretAnswer, String publicKey, String organization, OTPType otpType) throws UserExistsException, PolicyValidationException,
            BadPublicKeyException, MessageSendException {

        UserRegisterRequest registerUser = new UserRegisterRequest();
        registerUser.setUsername(username);
        registerUser.setEmail(email);
        registerUser.setSalt(saltSource.getSalt(username));
        registerUser.setHotpSalt(hotpSaltGenerator.generate());
        registerUser.setPassword(passwordEncoder.encode(password, registerUser.getSalt()));
        registerUser.setPrincipalNames(null);
        registerUser.setSubsystemName(subsystemIdentifier);
        registerUser.setName(name);
        registerUser.setSurname(surname);
        registerUser.setSecretQuestion(secretQuestion);
        registerUser.setSecretAnswer(secretAnswer);
        registerUser.setPublicKey(publicKey);
        registerUser.setOrganization(organization);
        registerUser.setOtpTypeCode(otpType.code());

        // validate password policy
        policyValidation.validate(new PasswordCheckContext(password, passwordEncoder, userService
                .getUserPasswordHistoryAndCurrentPassword(username)));

        validatePublicKey(publicKey);

        RoutineResult result = registerUserStrategy.registerUser(registerUser);
        if (result.isOk()) {
            for (RoleGrantSpecification roleGrant : roleGrants) {
                result = userService.grantRolesToUser(
                        registerUser.getUserid(),
                        roleGrant.isDetectSubsystemIdentifier() ? subsystemIdentifier : roleGrant
                                .getSubsystemIdentifier(), roleGrant.getPrincipalName());
                if (!result.isOk()) {
                    throw new IllegalStateException("Status: " + result.getStatus() + ", errorMessage: "
                            + result.getErrorMessage());
                }
            }
            notificationService.notifyAboutUsersChanged();
            loggerSink.info(logger, "REGISTER_USER", true, username);
        } else if (result.isDuplicate()) {
            loggerSink.info(logger, "REGISTER_USER", false, username);
            throw new UserExistsException(result.getErrorMessage());
        } else {
            loggerSink.info(logger, "REGISTER_USER", false, username);
            throw new IllegalStateException("Status: " + result.getStatus() + ", errorMessage: "
                    + result.getErrorMessage());
        }
    }

    private void validatePublicKey(String publicKey) throws BadPublicKeyException {
        PGPKeyValidator.validatePublicKey(publicKey, publicKeyCrypto);
    }

    @Override
    public void updateUserOtpType(String username, String otpType) {
        userService.updateUserOtpType(username, otpType);
    }

    @Override
    public void updateUserIsOtpOptionalValue(String username, boolean isOtpOptional) {
        userService.updateUserIsOtpOptionalValue(username, isOtpOptional);
    }

    @Override
    public boolean authenticateByOtpType(OTPType otp, String username, String code) {
        boolean ok = false;
        switch (otp) {
            case GOOGLE_AUTH:
                try {
                    ok = hotpService.validateGoogleTimePassword(username, code);
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
            userService.incrementHOTPLoginsFailed(username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.HOTP);
        } else {
            userService.clearHOTPLoginsFailed(username);
        }

        loggerSink.info(logger, "REMOTE_OTP_CHECK", ok, username);
        return ok;
    }

    protected SSOUserWithActions convertToSSOUser(UserWithActions user) {
        return new SSOUserWithActions(user.getUsername(), user.getEmail(), convertToSSOActions(user.getActions()));
    }

    public void changeTempPassword(String userName, String password) throws PolicyValidationException {
        policyValidation.validate(new PasswordCheckContext(password, passwordEncoder, userService
                .getUserPasswordHistoryAndCurrentPassword(userName)));
        userService.changeTempPassword(userName, password);
    }

    public UserForDescription getUserDescription(String username) {
        return userService.getUserForDescription(username);
    }

    public void updateUserForDescription(UserForDescription user) throws BadPublicKeyException {
        validatePublicKey(user.getPublicKey());
        userService.updateUserForDescription(user);
    }

    @Override
    public List<UserWithStatus> getUserStatuses(String userNames) {
        return userService.getUserStatuses(userNames);
    }

    @Override
    public SSOUser exchangeSubsystemToken(String subsystemToken) {
        SSOUser     ssoUser;
        AuthSession session = userService.exchangeSubsystemToken(subsystemToken);
        boolean     ok      = session != null && session.getSessionId() != null;
        loggerSink.info(logger, "EXCHANGE_SUBSYSTEM_TOKEN", ok, session != null ? session.getUsername() : "TOKEN: " + subsystemToken);
        if (ok) {
            ssoUser = buildSSOUser(session);
        } else {
            if (session != null) {
                logger.warn("No roles for user {}", session.getUsername());
            }
            ssoUser = null;
        }
        return ssoUser;
    }

    @Override
    public void touchSessions(List<Long> sessionIds) {
        if (sessionIds != null && !sessionIds.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Touching sessions " + sessionIds);
            }
            sessionService.touchSessions(StringUtils.collectionToCommaDelimitedString(sessionIds));
        }
    }

    @Override
    public void completeUser(String username) {
        userService.completeUser(username);
    }

    @Override
    public void changeUserRole(String username, String newRole, String subsystemIdentifier) {
        final RoutineResult result = userService.changeUserRole(username, newRole, subsystemIdentifier);
        if (!result.isOk()) {
            throw new IllegalStateException(result.getErrorMessage());
        }
    }

    @Override
    public boolean hasOtpMasterKey(String username) {
        return userService.getOtpMasterKeyByUsername(username) != null;
    }
}
