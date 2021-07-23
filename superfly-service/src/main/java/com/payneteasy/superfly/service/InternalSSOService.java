package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.BadPublicKeyException;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.MessageSendException;
import com.payneteasy.superfly.api.PolicyValidationException;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.model.UserWithStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;

/**
 * Internal service used to implement SSOService.
 * 
 * @author Roman Puchkovskiy
 */
public interface InternalSSOService {
    /**
     * Authenticates a user.
     *
     * @param username
     *            user name
     * @param password
     *            user password
     * @param subsystemIdentifier
     *            identifier of a subsystem from which user tries to log in
     * @param userIpAddress
     *            ID address of a user who tries to log in
     * @param sessionInfo
     *            session info
     * @return SSOUser instance on success or null on failure
     */
    SSOUser authenticate(String username, String password, String subsystemIdentifier, String userIpAddress,
            String sessionInfo);

    boolean checkOtp(SSOUser user, String code);

    /**
        * Returns the same data as if user was successfully authenticated,
     * but no actual authentication is made. This could be useful for
     * impersonation feature.
        *
        * @param username
        *            username to get user
        * @param subsystemIdentifier
        *            identifier of a subsystem
        * @return user object on success or null when retrieval fails
        */
       SSOUser pseudoAuthenticate(String username, String subsystemIdentifier);

    /**
     * Saves system data.
     *
     * @param subsystemIdentifier
     *            identifier of the system
     *            descriptions of roles
     * @param actionDescriptions
     *            descriptions of actions
     */
    void saveSystemData(String subsystemIdentifier, ActionDescription[] actionDescriptions);

    /**
     * Returns a list of users with their actions granted through role with the
     * given principal.
     *
     * @param subsystemIdentifier
     *            identifier of the subsystem from which users will be obtained
     * @return users with actions
     */
    List<SSOUserWithActions> getUsersWithActions(String subsystemIdentifier);

    /**
     * Registers a user.
     *
     * @param username
     *            name
     * @param password
     *            user's password
     * @param email
     *            user's email
     * @param subsystemIdentifier
     *            identifier of a subsystem to which he's to be given a role
     * @param roleGrants
     *            which roles to grant
     * @param publicKey
     *               user's public key
     * @param organization
     *               user's organization
     * @throws UserExistsException
     * @throws BadPublicKeyException
     * @throws MessageSendException
     */
    void registerUser(String username, String password, String email, String subsystemIdentifier,
            RoleGrantSpecification[] roleGrants, String name, String surname, String secretQuestion, String secretAnswer,
            String publicKey,String organization, OTPType otpType)
            throws UserExistsException, PolicyValidationException, BadPublicKeyException, MessageSendException;

    /**
     * Authenticates using HOTP (HMAC-based One Time Password).
     *
     * Deprecated. Use the {@link #authenticateByOtpType(OTPType, String, String)} authenticateByOtpType} method.
     *
     * @param username    username
     * @param hotp        HOTP
     * @return authentication result
     */
    @Deprecated
    boolean authenticateHOTP(String subsystemIdentifier, String username, String hotp);

    void updateUserOtpType(String username, String otpType);

    void updateUserIsOtpOptionalValue(String username, boolean isOtpOptional);

    /**
     * Authenticates using TOTP GoogleAuth
     *
     * @param username    username
     * @param key         key
     * @return authentication result
     */
    boolean authenticateByOtpType(OTPType otp, String username, String key);

    /**
     * 
     * @param userName user name
     * @param password password
     */
    void changeTempPassword(String userName, String password) throws PolicyValidationException;

    /**`
     * Returns a user description.
     * 
     * @param username    username
     * @return description
     */
    UserForDescription getUserDescription(String username);

    /**
     * Updates user's fields.
     *
     * @param user    user's fields
     */
    void updateUserForDescription(UserForDescription user) throws BadPublicKeyException;

    /**
     * Finds users by comma-separated list of their logins and returns their status information.
     *
     * @param userNames comma-separated list of user names (logins)
     * @return user statuses
     */
    List<UserWithStatus> getUserStatuses(String userNames);

    /**
     * Exchanges subsystem token to SSOUser. After this operation
     * returns, subsystem token is not valid anymore and cannot
     * be used for exchanging.
     *
     * @param subsystemToken    subsystem token
     * @return SSOUser or null if token does not exist, expired or
     * already used
     */
    SSOUser exchangeSubsystemToken(String subsystemToken);

    /**
     * Touches sessions: that is, updates their access time to avoid
     * removal. If a session was issued by an SSO session, the latter
     * is touched too.
     *
     * @param sessionIds    IDs of sessions to touch
     */
    void touchSessions(List<Long> sessionIds);

    /**
     * Makes a user complete.
     *
     * @param username  name of the user to complete
     */
    void completeUser(String username);

    /**
     * Revokes from a user all his roles and replaces them with a given role.
     *
     * @param username              name of the user to work with
     * @param newRole               role to grant
     * @param subsystemIdentifier   identifier of the subsystem which roles
     *                              are mentioned
     */
    void changeUserRole(String username, String newRole, String subsystemIdentifier);

    boolean hasOtpMasterKey(String username);
}
