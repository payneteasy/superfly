package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.*;
import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.*;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of SSOService.
 *
 * @author Roman Puchkovskiy
 */
@Service
@RequiredArgsConstructor
public class SSOServiceImpl implements SSOService {
    @Setter
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
    public boolean checkOtp(CheckOtpRequest request) {
        return internalSSOService.checkOtp(request.getUser(), request.getCode());
    }

    @Override
    public boolean hasOtpMasterKey(HasOtpMasterKeyRequest request) {
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
        internalSSOService.updateUserOtpType(request.getUsername(), request.getOtpType());
    }

    @Override
    public void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException {
        internalSSOService.changeTempPassword(request.getUsername(), request.getNewPassword());
    }

    /**
     * @see SSOService#getUserDescription(GetUserDescriptionRequest)
     */
    @Override
    public UserDescription getUserDescription(GetUserDescriptionRequest request) {
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
        result.setSecretAnswer(user.getSecretAnswer());
        result.setPublicKey(user.getPublicKey());
        result.setOtpOptional(user.isOtpOptional());
        result.setOtpType(user.getOtpType());
        result.setOrganization(user.getOrganization());

        return result;
    }

    @Override
    public String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request)
            throws UserNotFoundException, SsoDecryptException {
        String subsystemIdentifier = obtainSubsystemIdentifier(null);
        return hotpService.resetGoogleAuthMasterKey(subsystemIdentifier, request.getUsername());
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
        userForDescription.setSecretAnswer(user.getSecretAnswer());
        userForDescription.setPublicKey(user.getPublicKey());
        userForDescription.setOrganization(user.getOrganization());

        internalSSOService.updateUserForDescription(userForDescription);
    }

    private void doResetPassword(String username,
                                 String newPassword,
                                 boolean sendPasswordByEmail
    ) throws UserNotFoundException {
        UserForDescription user = internalSSOService.getUserDescription(username);
        if (user == null) {
            throw new UserNotFoundException(username);
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
     * @see SSOService#resetPassword(com.payneteasy.superfly.api.PasswordReset)
     */
    @Override
    public void resetPassword(PasswordReset reset)
            throws UserNotFoundException, PolicyValidationException {
        doResetPassword(reset.getUsername(), reset.getPassword(), reset.isSendByEmail());
    }

    @Override
    public List<UserStatus> getUserStatuses(GetUserStatusesRequest request) {
        List<UserWithStatus> daoUsers;
        if (request.getUserNames() == null) {
            daoUsers = internalSSOService.getUserStatuses(null);
        } else if (request.getUserNames().isEmpty()) {
            daoUsers = Collections.emptyList();
        } else {
            daoUsers = internalSSOService.getUserStatuses(
                    StringUtils.collectionToCommaDelimitedString(request.getUserNames())
            );
        }

        List<UserStatus> result = new ArrayList<>(daoUsers.size());
        for (UserWithStatus daoUser : daoUsers) {
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
        return internalSSOService.exchangeSubsystemToken(request.getSubsystemToken());
    }

    @Override
    public void touchSessions(TouchSessionsRequest request) {
        internalSSOService.touchSessions(request.getSessionIds());
    }

    @Override
    public void completeUser(CompleteUserRequest request) {
        internalSSOService.completeUser(request.getUsername());
    }

    @Override
    public void changeUserRole(ChangeUserRoleRequest request) {
        internalSSOService.changeUserRole(
                request.getUsername(),
                request.getNewRole(),
                obtainSubsystemIdentifier(request.getSubsystemHint())
        );
    }

    protected String obtainSubsystemIdentifier(String systemIdentifier) {
        return subsystemIdentifierObtainer.obtainSubsystemIdentifier(systemIdentifier);
    }
}
