package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.GetEventsRequest;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.UserRegisterRequest;
import com.payneteasy.superfly.model.*;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.Pbkdf2PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.impl.AbstractPolicyValidation;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.PasswordSaltPair;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.service.*;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import com.payneteasy.superfly.utils.PGPKeyValidator;
import com.payneteasy.superfly.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private       PasswordEncoder      legacyPasswordEncoder;
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
        String  salt = saltSource.getSalt(username);
        // null password is an ordinary failed attempt, not an exception
        AuthSession session = userService.authenticate(username,
                password == null ? Pbkdf2PasswordEncoder.NEVER_MATCHING_HASH : passwordEncoder.encode(password, salt),
                password == null ? null : legacyPasswordEncoder.encode(password, salt),
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

    @Override
    public CheckOtpResult.Status checkOtp(OTPType otpType, boolean isOtpOptional, String username, String code) {
        // isOtpOptional comes from the caller; a configured key makes OTP mandatory anyway
        if (isOtpOptional && (code == null || code.trim().isEmpty()) && !hasOtpMasterKey(username)) {
            return CheckOtpResult.Status.SUCCESS;
        }
        return authenticateByOtpType(otpType, username, code);
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
        // effective flag: clients decide whether to ask for a code from it, and a configured key makes OTP mandatory
        ssoUser.setOtpOptional(session.isOtpOptional()
                && !(session.otpType() == OTPType.GOOGLE_AUTH && hasOtpMasterKey(session.getUsername())));
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
        policyValidation.validate(new PasswordCheckContext(password, legacyPasswordEncoder, userService
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
    public CheckOtpResult.Status authenticateByOtpType(OTPType otp, String username, String code) {
        logger.debug("Authenticating by OTP type {} for {}", otp, username);
        Objects.requireNonNull(otp);
        CheckOtpResult.Status status;
        if (isAccountLocked(username)) {
            // a locked account gets no code checks: they would let the code be guessed without consequences
            logger.warn("OTP check of locked account {}", username);
            status = CheckOtpResult.Status.LOCKED;
        } else {
            status = otp == OTPType.GOOGLE_AUTH
                    ? hotpService.validateGoogleTimePassword(username, code)
                    : CheckOtpResult.Status.SUCCESS;
            if (status != CheckOtpResult.Status.SUCCESS) {
                logger.warn("OTP check failed {}: {}", username, status);
                status = countFailedOtpAttempt(username, status);
            } else {
                userService.clearHOTPLoginsFailed(username);
            }
        }

        loggerSink.info(logger, "REMOTE_OTP_CHECK", status == CheckOtpResult.Status.SUCCESS, username, "status=" + status);
        return status;
    }

    @Override
    public CheckOtpResult.Status confirmOtpMasterKey(String username, String code) {
        CheckOtpResult.Status status;
        if (isAccountLocked(username)) {
            logger.warn("OTP key confirmation of locked account {}", username);
            status = CheckOtpResult.Status.LOCKED;
        } else {
            status = hotpService.confirmGoogleAuthMasterKey(username, code);
            // otherwise confirmation attempts would guess codes past the OTP lockout limit
            if (status != CheckOtpResult.Status.SUCCESS) {
                logger.warn("OTP key confirmation failed {}: {}", username, status);
                status = countFailedOtpAttempt(username, status);
            }
        }

        loggerSink.info(logger, "REMOTE_OTP_KEY_CONFIRM", status == CheckOtpResult.Status.SUCCESS, username, "status=" + status);
        return status;
    }

    /** @return {@link CheckOtpResult.Status#LOCKED} if this attempt locked the account, the given status otherwise */
    private CheckOtpResult.Status countFailedOtpAttempt(String username, CheckOtpResult.Status status) {
        userService.incrementHOTPLoginsFailed(username);
        lockoutStrategy.checkLoginsFailed(username, LockoutType.HOTP);
        return isAccountLocked(username) ? CheckOtpResult.Status.LOCKED : status;
    }

    private boolean isAccountLocked(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        // The key lookup finds the user with "=", which ignores trailing spaces and compares by collation, while
        // get_user_statuses matches the list with instr: take the stored name from a lookup with the same "=".
        UserForDescription user = userService.getUserForDescription(username);
        String storedName = user == null ? null : user.getUsername();
        // an empty list means "all users" to the procedure
        if (storedName == null || storedName.isEmpty()) {
            return false;
        }
        // the procedure splits its argument on commas, so only the row of this very user counts
        List<UserWithStatus> statuses = userService.getUserStatuses(storedName);
        return statuses != null && statuses.stream()
                .anyMatch(status -> storedName.equals(status.getUserName()) && status.isAccountLocked());
    }

    protected SSOUserWithActions convertToSSOUser(UserWithActions user) {
        return new SSOUserWithActions(user.getUsername(), user.getEmail(), convertToSSOActions(user.getActions()));
    }

    public void changeTempPassword(String userName, String password) throws PolicyValidationException {
        policyValidation.validate(new PasswordCheckContext(password, legacyPasswordEncoder, userService
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
    public SSOUser exchangeSubsystemToken(String subsystemToken, String callerSubsystem) {
        SSOUser     ssoUser;
        AuthSession session = userService.exchangeSubsystemToken(subsystemToken, callerSubsystem);
        boolean     ok      = session != null && session.getSessionId() != null;
        loggerSink.info(logger, "EXCHANGE_SUBSYSTEM_TOKEN", ok, session != null ? session.getUsername() : "TOKEN: ***");
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
    public void touchSessions(List<Long> sessionIds, String subsystemIdentifier) {
        if (sessionIds != null && !sessionIds.isEmpty() && subsystemIdentifier != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Touching {} sessions for subsystem {}", sessionIds.size(), subsystemIdentifier);
            }
            sessionService.touchSessions(StringUtils.collectionToCommaDelimitedString(sessionIds), subsystemIdentifier);
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

    @Override
    public boolean userHasRolesInSubsystem(String username, String subsystemIdentifier) {
        return userService.userHasRolesInSubsystem(username, subsystemIdentifier);
    }

    @Override
    public void validatePasswordPolicy(String username, String password) throws PolicyValidationException {
        List<PasswordSaltPair> history = username == null
                ? Collections.emptyList()
                : userService.getUserPasswordHistoryAndCurrentPassword(username);
        policyValidation.validate(new PasswordCheckContext(password, legacyPasswordEncoder, history));
    }

    private EventService eventService;

    @Autowired
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<SSOEvent> getEvents(Long lastEventId, long waitTimeMs, String subsystemIdentifier) {
        List<Event> events = eventService.getEvents(lastEventId, waitTimeMs, subsystemIdentifier);
        if (events != null && !events.isEmpty()) {
            logger.info("getEvents call info={}", events);
            return events.stream()
                    .map(event -> new SSOEvent(event.getEventId(), event.getEventTime(), event.getEventTypeCode(), event.getEventData()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public long getLastEventId(String subsystemIdentifier) {
        return eventService.getLastEventId(subsystemIdentifier);
    }
}
