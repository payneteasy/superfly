package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.common.utils.UserNames;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.utils.StringUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of SSOService.
 *
 * @author Roman Puchkovskiy
 */
@Service
@RequiredArgsConstructor
public class SSOServiceImpl implements SSOService {
    private static final Logger logger = LoggerFactory.getLogger(SSOServiceImpl.class);

    @Setter
    @Autowired(required = false)
    private SubsystemIdentifierObtainer subsystemIdentifierObtainer = new AuthRequestInfoObtainer();
    private final InternalSSOService          internalSSOService;
    private final HOTPService                 hotpService;
    private final ResetPasswordStrategy       resetPasswordStrategy;
    private final EmailService                emailService;
    private final PublicKeyCrypto             publicKeyCrypto;

    /**
     * @see SSOService#authenticate(AuthenticateRequest)
     */
    @Override
    public SSOUser authenticate(AuthenticateRequest request) {
        return internalSSOService.authenticate(
                request.getUsername(),
                request.getPassword(),
                obtainSubsystemIdentifier(request.getAuthRequestInfo().getSubsystemIdentifier()),
                request.getAuthRequestInfo().getIpAddress(),
                request.getAuthRequestInfo().getSessionInfo()
        );
    }

    @Override
    public CheckOtpResult checkOtp(CheckOtpRequest request) throws SsoDecryptException {
        if (!isUserAccessible("checkOtp", request.getUserName())) {
            return new CheckOtpResult(checkOtpForUnknownUser(request));
        }
        return new CheckOtpResult(internalSSOService.checkOtp(request.getOtpType(), request.isOtpOptional(), request.getUserName(),  request.getCode()));
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) {
        if (!isUserAccessible("hasOtpMasterKey", request.getUsername())) {
            return false;
        }
        return internalSSOService.hasOtpMasterKey(request.getUsername());
    }

    /**
     * @see SSOService#pseudoAuthenticate(PseudoAuthenticateRequest)
     */
    @Override
    public SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request) {
        return internalSSOService.pseudoAuthenticate(
                request.getUsername(),
                obtainSubsystemIdentifier(request.getSubsystemIdentifier())
        );
    }

    /**
     * @see SSOService#sendSystemData(SendSystemDataRequest)
     */
    @Override
    public void sendSystemData(SendSystemDataRequest request) {
        internalSSOService.saveSystemData(
                obtainSubsystemIdentifier(request.getSubsystemIdentifier()),
                request.getActionDescriptions().toArray(new ActionDescription[0])
        );
    }

    /**
     * @see SSOService#getUsersWithActions(GetUsersWithActionsRequest)
     */
    @Override
    public List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request) {
        return internalSSOService.getUsersWithActions(
                obtainSubsystemIdentifier(request.getSubsystemIdentifier())
        );
    }

    @Override
    public void registerUser(UserRegisterRequest registerRequest)
            throws UserExistsException, PolicyValidationException, BadPublicKeyException, MessageSendException {
        checkRoleGrantSubsystems(registerRequest.getRoleGrants());
        internalSSOService.registerUser(registerRequest.getUsername(),
                                        registerRequest.getPassword(),
                                        registerRequest.getEmail(),
                                        obtainSubsystemIdentifier(registerRequest.getSubsystemHint()),
                                        registerRequest.getRoleGrants(),
                                        registerRequest.getFirstName(),
                                        registerRequest.getLastName(),
                                        registerRequest.getSecretQuestion(),
                                        registerRequest.getSecretAnswer(),
                                        registerRequest.getPublicKey(),
                                        registerRequest.getOrganization(),
                                        registerRequest.getOtpType()
        );
    }

    @Override
    public void updateUserOtpType(UpdateUserOtpTypeRequest request) {
        if (!isUserManageable("updateUserOtpType", request.getUsername())) {
            return;
        }
        internalSSOService.updateUserOtpType(request.getUsername(), request.getOtpType());
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        if (!isUserManageable("changeTempPassword", request.getUsername())) {
            internalSSOService.validatePasswordPolicy(null, request.getNewPassword());
            return;
        }
        internalSSOService.changeTempPassword(request.getUsername(), request.getNewPassword());
    }

    /**
     * @see SSOService#getUserDescription(GetUserDescriptionRequest)
     */
    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) {
        if (!isUserAccessible("getUserDescription", request.getUsername())) {
            return null;
        }
        UserForDescription user = internalSSOService.getUserDescription(request.getUsername());
        if (user == null) {
            return null;
        }

        UserDescription result = new UserDescription();
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());
        result.setFirstName(user.getName());
        result.setLastName(user.getSurname());
        result.setSecretQuestion(user.getSecretQuestion());
        // the answer is a credential that Superfly never checks: subsystems do not need it
        result.setPublicKey(user.getPublicKey());
        result.setOtpOptional(user.isOtpOptional());
        result.setOtpType(user.getOtpType());
        result.setOrganization(user.getOrganization());

        return result;
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        if (!isUserManageable("resetGoogleAuthMasterKey", request.getUsername())) {
            // an unknown user gets a fresh key that is persisted nowhere
            return new GoogleAuthenticator().createCredentials().getKey();
        }
        String subsystemIdentifier = obtainSubsystemIdentifier(null);
        return hotpService.resetGoogleAuthMasterKey(subsystemIdentifier, request.getUsername());
    }

    @Override
    public CheckOtpResult confirmOtpMasterKey(ConfirmOtpMasterKeyRequest request) throws SsoDecryptException {
        if (!isUserManageable("confirmOtpMasterKey", request.getUsername())) {
            return new CheckOtpResult(CheckOtpResult.Status.INVALID);
        }
        return new CheckOtpResult(internalSSOService.confirmOtpMasterKey(request.getUsername(), request.getCode()));
    }

    @Override
    public String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request) {
        return hotpService.getUrlToGoogleAuthQrCode(
                request.getSecretKey(),
                request.getIssuer(),
                request.getAccountName()
        );
    }

    @Override
    public void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request) {
        if (!isUserManageable("updateUserIsOtpOptionalValue", request.getUsername())) {
            return;
        }
        internalSSOService.updateUserIsOtpOptionalValue(
                request.getUsername(),
                request.isOtpOptional()
        );
    }

    /**
     * @see SSOService#updateUserDescription(UpdateUserDescriptionRequest)
     */
    @Override
    public void updateUserDescription(UpdateUserDescriptionRequest request)
            throws UserNotFoundException, BadPublicKeyException {
        if (!isUserManageable("updateUserDescription", request.getUserDescription().getUsername())) {
            throw new UserNotFoundException(request.getUserDescription().getUsername());
        }
        UserForDescription userForDescription = internalSSOService.getUserDescription(
                request.getUserDescription().getUsername()
        );
        if (userForDescription == null) {
            throw new UserNotFoundException(request.getUserDescription().getUsername());
        }

        UserDescription user = request.getUserDescription();
        userForDescription.setEmail(user.getEmail());
        userForDescription.setName(user.getFirstName());
        userForDescription.setSurname(user.getLastName());
        userForDescription.setSecretQuestion(user.getSecretQuestion());
        // getUserDescription does not return the answer, so a read-modify-write sends null: keep the stored one
        if (user.getSecretAnswer() != null) {
            userForDescription.setSecretAnswer(user.getSecretAnswer());
        }
        userForDescription.setPublicKey(user.getPublicKey());
        userForDescription.setOrganization(user.getOrganization());

        internalSSOService.updateUserForDescription(userForDescription);
    }

    private void doResetPassword(String username,
                                 String newPassword,
                                 boolean sendPasswordByEmail
    ) throws UserNotFoundException, PolicyValidationException {
        if (!isUserManageable("resetPassword", username)) {
            throw new UserNotFoundException(username);
        }
        UserForDescription user = internalSSOService.getUserDescription(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        if (newPassword != null) {
            internalSSOService.validatePasswordPolicy(username, newPassword);
        }
        resetPasswordStrategy.resetPassword(user.getUserId(), username, newPassword);
        if (sendPasswordByEmail && user.getPublicKey() != null) {
            // TODO: we could factor this code out to some
            // service method to use it also when resetting password
            // using Superfly UI and other means, but it's not clear
            // how to get SMTP server when subsystem is not known
            // This is to be resolved later
            String                subsystemIdentifier = obtainSubsystemIdentifier(null); // TODO: take default from API?
            ByteArrayOutputStream baos                = new ByteArrayOutputStream();
            final String          fileName            = "password.txt";
            try {
                publicKeyCrypto.encrypt(getStringBytes(newPassword), fileName, user.getPublicKey(), baos);
            } catch (IOException e) {
                // should not happen as we encrypt in memory
                throw new IllegalStateException("Should not happen", e);
            }
            emailService.sendPassword(subsystemIdentifier, user.getEmail(), fileName, baos.toByteArray());
        }
    }

    private byte[] getStringBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @see SSOService#resetPassword(PasswordResetRequest)
     */
    @Override
    public void resetPassword(PasswordResetRequest reset)
            throws UserNotFoundException, PolicyValidationException {
        doResetPassword(reset.getUsername(), reset.getPassword(), reset.isSendByEmail());
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) {
        // null means "everyone" for the DAO, which would expose foreign users: an explicit list is required
        List<String> userNames = request.getUserNames() == null
                ? Collections.emptyList()
                : request.getUserNames().stream()
                        // the DAO splits the argument by commas, so such a name could address another user
                        .filter(name -> name != null && !name.contains(","))
                        .filter(name -> isUserAccessible("getUserStatuses", name))
                        .collect(Collectors.toList());
        List<UserWithStatus> daoUsers;
        if (userNames.isEmpty()) {
            daoUsers = Collections.emptyList();
        } else {
            daoUsers = internalSSOService.getUserStatuses(
                    StringUtils.collectionToCommaDelimitedString(userNames)
            );
        }

        List<UserStatus> result = new ArrayList<>(daoUsers.size());
        for (UserWithStatus daoUser : daoUsers) {
            if (userNames.stream().noneMatch(name -> name.equalsIgnoreCase(daoUser.getUserName()))) {
                continue;
            }
            UserStatus user = new UserStatus();
            user.setUsername(daoUser.getUserName());
            user.setAccountLocked(daoUser.isAccountLocked());
            user.setLoginsFailed(daoUser.getLoginsFailed());
            user.setLastLoginDate(daoUser.getLastLoginDate());
            user.setLastFailedLoginDate(daoUser.getLastFailedLoginDate());
            user.setLastFailedLoginIp(daoUser.getLastFailedLoginIp());
            result.add(user);
        }
        return result;
    }

    @Override
    public SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request) {
        // the token is bound to the calling subsystem; a caller without one cannot exchange anything
        String callerSubsystem = obtainSubsystemIdentifier(null);
        if (callerSubsystem == null) {
            return null;
        }
        return internalSSOService.exchangeSubsystemToken(request.getSubsystemToken(), callerSubsystem);
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) {
        internalSSOService.touchSessions(request.getSessionIds(), obtainSubsystemIdentifier(null));
    }

    @Override
    public void completeUser(CompleteUserRequest request) {
        if (!isUserManageable("completeUser", request.getUsername())) {
            return;
        }
        internalSSOService.completeUser(request.getUsername());
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) {
        if (!isUserManageable("changeUserRole", request.getUsername())) {
            // same exception as InternalSSOServiceImpl gives for an unknown user
            throw new IllegalStateException("Cannot find user by name");
        }
        internalSSOService.changeUserRole(
                request.getUsername(),
                request.getNewRole(),
                obtainSubsystemIdentifier(request.getSubsystemHint())
        );
    }

    @Override
    public List<SSOEvent> getEvents(GetEventsRequest request) {
        return internalSSOService.getEvents(request.getLastEventId(), request.getWaitTimeMs(),
                obtainSubsystemIdentifier(request.getSubsystemName()));
    }

    @Override
    public Long getLastEventId() {
        return internalSSOService.getLastEventId(obtainSubsystemIdentifier(null));
    }

    /**
     * A subsystem may sign in and read only users that have a role in it. A denial must look like
     * "no such user" to the caller, so callers mimic the unknown-user behaviour of their method.
     * A name that cannot exist (empty, longer than the column) is such a user too and never reaches the database.
     */
    private boolean isUserAccessible(String method, String username) {
        String subsystem = obtainSubsystemIdentifier(null);
        if (!UserNames.isPossible(username) || !internalSSOService.isUserAccessibleFrom(username, subsystem)) {
            logDenied(method, subsystem, username);
            return false;
        }
        return true;
    }

    /**
     * Changes additionally exclude users with a role in the local (admin UI) subsystem: their password,
     * OTP, profile and roles are changed in Superfly only. The denial looks like "no such user" as well.
     */
    private boolean isUserManageable(String method, String username) {
        String subsystem = obtainSubsystemIdentifier(null);
        if (!UserNames.isPossible(username) || !internalSSOService.isUserManageableFrom(username, subsystem)) {
            logDenied(method, subsystem, username);
            return false;
        }
        return true;
    }

    private void logDenied(String method, String subsystem, String username) {
        logger.warn("Subsystem {} was denied {} on user {}", sanitize(subsystem), method, sanitize(username));
    }

    // values come from the request body: strip line breaks to prevent log injection
    private static String sanitize(String value) {
        return value == null ? null : value.replaceAll("[\\r\\n\\t]", "_");
    }

    /**
     * What the underlying service does for a user that does not exist: never a status that only an existing
     * user can get (locked, already used, clock skew).
     */
    private CheckOtpResult.Status checkOtpForUnknownUser(CheckOtpRequest request) throws SsoDecryptException {
        String code = request.getCode();
        if (request.isOtpOptional() && (code == null || code.trim().isEmpty())) {
            return CheckOtpResult.Status.SUCCESS;
        }
        // the real path fails on a missing type with the same NPE
        if (Objects.requireNonNull(request.getOtpType()) != OTPType.GOOGLE_AUTH) {
            return CheckOtpResult.Status.SUCCESS;
        }
        if (code != null && code.matches("^[0-9]{6}$")) {
            throw new SsoDecryptException("GA master key for " + request.getUserName() + " is null");
        }
        return CheckOtpResult.Status.INVALID;
    }

    /**
     * Explicit subsystem identifiers inside role grants bypass the hint, so they must pass the
     * same check; the obtainer throws if the caller is a subsystem and the identifier is foreign.
     */
    private void checkRoleGrantSubsystems(RoleGrantSpecification[] roleGrants) {
        if (roleGrants == null) {
            return;
        }
        for (RoleGrantSpecification grant : roleGrants) {
            if (grant != null && !grant.isDetectSubsystemIdentifier() && grant.getSubsystemIdentifier() != null) {
                obtainSubsystemIdentifier(grant.getSubsystemIdentifier());
            }
        }
    }

    protected String obtainSubsystemIdentifier(String systemIdentifier) {
        return subsystemIdentifierObtainer.obtainSubsystemIdentifier(systemIdentifier);
    }

}
