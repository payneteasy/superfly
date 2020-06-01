package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.BadPublicKeyException;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.PasswordReset;
import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserDescription;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.api.UserNotFoundException;
import com.payneteasy.superfly.api.UserRegisterRequest;
import com.payneteasy.superfly.api.UserStatus;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.email.EmailService;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.spisupport.HOTPService;
import com.payneteasy.superfly.utils.StringUtils;
import org.springframework.beans.factory.annotation.Required;

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
public class SSOServiceImpl implements SSOService {

    private InternalSSOService internalSSOService;
    private HOTPService hotpService;
    private ResetPasswordStrategy resetPasswordStrategy;
    private SubsystemIdentifierObtainer subsystemIdentifierObtainer = new AuthRequestInfoObtainer();
    private EmailService emailService;
    private PublicKeyCrypto publicKeyCrypto;

    @Required
    public void setInternalSSOService(InternalSSOService internalSSOService) {
        this.internalSSOService = internalSSOService;
    }

    @Required
    public void setHotpService(HOTPService hotpService) {
        this.hotpService = hotpService;
    }

    @Required
    public void setResetPasswordStrategy(ResetPasswordStrategy resetPasswordStrategy) {
        this.resetPasswordStrategy = resetPasswordStrategy;
    }

    public void setSubsystemIdentifierObtainer(
            SubsystemIdentifierObtainer subsystemIdentifierObtainer) {
        this.subsystemIdentifierObtainer = subsystemIdentifierObtainer;
    }

    @Required
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Required
    public void setPublicKeyCrypto(PublicKeyCrypto publicKeyCrypto) {
        this.publicKeyCrypto = publicKeyCrypto;
    }

    /**
     * @see SSOService#authenticate(String, String, AuthenticationRequestInfo)
     */
    @Override
    public SSOUser authenticate(String username, String password,
            AuthenticationRequestInfo authRequestInfo) {
        return internalSSOService.authenticate(username, password,
                obtainSubsystemIdentifier(authRequestInfo.getSubsystemIdentifier()),
                authRequestInfo.getIpAddress(),
                authRequestInfo.getSessionInfo());
    }

    @Override
    public boolean checkOtp(SSOUser user, String code) {
        return internalSSOService.checkOtp(user, code);
    }

    /**
     * @see SSOService#pseudoAuthenticate(String, String)
     */
    @Override
    public SSOUser pseudoAuthenticate(String username, String subsystemIdentifier) {
        return internalSSOService.pseudoAuthenticate(username, obtainSubsystemIdentifier(subsystemIdentifier));
    }

    /**
     * @see SSOService#sendSystemData(String, com.payneteasy.superfly.api.ActionDescription[])
     */
    @Override
    public void sendSystemData(String systemIdentifier,
            ActionDescription[] actionDescriptions) {
        internalSSOService.saveSystemData(obtainSubsystemIdentifier(systemIdentifier),
                actionDescriptions);
    }

    /**
     * @see SSOService#getUsersWithActions(String)
     */
    @Override
    public List<SSOUserWithActions> getUsersWithActions(
            String subsystemIdentifier) {
        return internalSSOService.getUsersWithActions(
                obtainSubsystemIdentifier(subsystemIdentifier));
    }

    /**
     * @see SSOService#registerUser(String, String, String, String, com.payneteasy.superfly.api.RoleGrantSpecification[], String, String, String, String, String, String, OTPType)
     * @deprecated use #registerUser(UserRegisterRequest) instead
     */
    @Override
    @Deprecated
    public void registerUser(String username, String password, String email,
            String subsystemIdentifier, RoleGrantSpecification[] roleGrants,
            String name, String surname, String secretQuestion, String secretAnswer,
            String publicKey, String organization, OTPType otpType)
            throws UserExistsException, PolicyValidationException, BadPublicKeyException, MessageSendException {
        internalSSOService.registerUser(username, password, email,
                                        obtainSubsystemIdentifier(subsystemIdentifier), roleGrants,
                                        name, surname, secretQuestion, secretAnswer, publicKey, organization, otpType);
    }

    /**
     * @see SSOService#registerUser(com.payneteasy.superfly.api.UserRegisterRequest)
     */
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
                registerRequest.getOtpType());
    }

    /**
     * @see SSOService#authenticateUsingHOTP(String, String)
     */
    @Override
    public boolean authenticateUsingHOTP(String username, String hotp) {
        String subsystemIdentifier = obtainSubsystemIdentifier(null); // TODO: take default from API
        return internalSSOService.authenticateHOTP(subsystemIdentifier, username, hotp);
    }

    @Override
    public void updateUserOtpType(String username, String otpType) {
        internalSSOService.updateUserOtpType(username, otpType);
    }

    @Override
    public boolean authenticateUsingGoogleAuth(String username, String key) throws SsoDecryptException {
        return internalSSOService.authenticateTOTPGoogleAuth(username, key);
    }

    protected String obtainSubsystemIdentifier(String systemIdentifier) {
        return subsystemIdentifierObtainer.obtainSubsystemIdentifier(systemIdentifier);
    }

    @Override
    public void changeTempPassword(String userName, String password) throws PolicyValidationException {
        internalSSOService.changeTempPassword(userName, password);
    }

    /**
     * @see SSOService#getUserDescription(String)
     */
    @Override
    public UserDescription getUserDescription(String username) {
        UserDescription result;
        UserForDescription user = internalSSOService.getUserDescription(username);
        if (user == null) {
            result = null;
        } else {
            result = new UserDescription();
            result.setUsername(user.getUsername());
            result.setEmail(user.getEmail());
            result.setFirstName(user.getName());
            result.setLastName(user.getSurname());
            result.setSecretQuestion(user.getSecretQuestion());
            result.setSecretAnswer(user.getSecretAnswer());
            result.setPublicKey(user.getPublicKey());
        }

        return result;
    }

    /**
     * @see SSOService#resetAndSendOTPTable(String)
     */
    @Override
    public void resetAndSendOTPTable(String username) throws UserNotFoundException, MessageSendException {
        resetAndSendOTPTable(null, username);
    }

    /**
     * @see SSOService#resetAndSendOTPTable(String, String)
     */
    @Override
    public void resetAndSendOTPTable(String subsystemIdentifier,
            String username) throws UserNotFoundException, MessageSendException {
        UserForDescription user = internalSSOService.getUserDescription(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        hotpService.sendTableIfSupported(obtainSubsystemIdentifier(subsystemIdentifier), user.getUserId());
    }

    @Override
    public String resetGoogleAuthMasterKey(String username) throws UserNotFoundException, SsoDecryptException {
        String subsystemIdentifier = obtainSubsystemIdentifier(null); // TODO: take default from API
        return hotpService.resetGoogleAuthMasterKey(subsystemIdentifier, username);
    }

    @Override
    public String getUrlToGoogleAuthQrCode(String secretKey, String issuer, String accountName) {
        return hotpService.getUrlToGoogleAuthQrCode(secretKey, issuer, accountName);
    }

    /**
     * @see SSOService#updateUserDescription(UserDescription)
     */
    @Override
    public void updateUserDescription(UserDescription user)
            throws UserNotFoundException, BadPublicKeyException {
        UserForDescription userForDescription = internalSSOService.getUserDescription(user.getUsername());
        if (userForDescription == null) {
            throw new UserNotFoundException(user.getUsername());
        }
        userForDescription.setEmail(user.getEmail());
        userForDescription.setName(user.getFirstName());
        userForDescription.setSurname(user.getLastName());
        userForDescription.setSecretQuestion(user.getSecretQuestion());
        userForDescription.setSecretAnswer(user.getSecretAnswer());
        userForDescription.setPublicKey(user.getPublicKey());
        userForDescription.setOrganization(user.getOrganization());
        internalSSOService.updateUserForDescription(userForDescription);
    }

    /**
     * @see SSOService#resetPassword(String, String)
     */
    @Override
    public void resetPassword(String username, String newPassword)
            throws UserNotFoundException, PolicyValidationException {
        doResetPassword(username, newPassword, false);
    }

    private void doResetPassword(String username, String newPassword,
            boolean sendPasswordByEmail) throws UserNotFoundException {
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
            String subsystemIdentifier = obtainSubsystemIdentifier(null); // TODO: take default from API?
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final String fileName = "password.txt";
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
    public List<UserStatus> getUserStatuses(List<String> userNames) {
        List<UserWithStatus> daoUsers;
        if (userNames == null) {
            daoUsers = internalSSOService.getUserStatuses(null);
        } else if (userNames.isEmpty()) {
            daoUsers = Collections.emptyList();
        } else {
            daoUsers = internalSSOService.getUserStatuses(
                    StringUtils.collectionToCommaDelimitedString(userNames));
        }
        List<UserStatus> result = new ArrayList<UserStatus>(daoUsers.size());
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
    public SSOUser exchangeSubsystemToken(String subsystemToken) {
        return internalSSOService.exchangeSubsystemToken(subsystemToken);
    }

    @Override
    public void touchSessions(List<Long> sessionIds) {
        internalSSOService.touchSessions(sessionIds);
    }

    @Override
    public void completeUser(String username) {
        internalSSOService.completeUser(username);
    }

    @Override
    public void changeUserRole(String username, String newRole) {
        changeUserRole(username, newRole, null);
    }

    @Override
    public void changeUserRole(String username, String newRole, String subsystemHint) {
        internalSSOService.changeUserRole(username, newRole, obtainSubsystemIdentifier(subsystemHint));
    }
}
