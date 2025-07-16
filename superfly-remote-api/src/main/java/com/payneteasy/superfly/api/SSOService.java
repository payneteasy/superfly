package com.payneteasy.superfly.api;

import com.payneteasy.superfly.api.exceptions.*;
import com.payneteasy.superfly.api.request.*;

import java.util.List;

/**
 * Service for SSO communication.
 *
 * @author Roman Puchkovskiy
 * @since 1.0
 */
public interface SSOService {
    /**
     * Attempts to authenticate user from the current system using the supplied
     * username and password. If successful, returns user object, else returns
     * null.
     *
     * @param authenticate authenticate request containing username and password
     * @return user object on success or null when authentication failed (for
     * instance, no such user, or password mismatched, or user is
     * blocked...)
     */
    SSOUser authenticate(AuthenticateRequest authenticate);

    boolean checkOtp(CheckOtpRequest request) throws SsoDecryptException;

    boolean hasOtpMasterKey(HasOtpMasterKeyRequest request);

    /**
     * Returns the same data as if user was successfully authenticated,
     * but no actual authentication is made. This could be useful for
     * impersonation feature.
     *
     * @param request pseudo authenticate request containing username and subsystem identifier
     * @return user object on success or null when retrieval fails (for
     * instance, no such user)
     * @since 1.5
     */
    SSOUser pseudoAuthenticate(PseudoAuthenticateRequest request);

    /**
     * Sends data about this system to SSO server.
     *
     * @param request send system data request containing subsystem identifier and action descriptions
     */
    void sendSystemData(SendSystemDataRequest request);

    /**
     * Returns a list of users with their actions granted through role with the
     * given principal.
     *
     * @param request get users with actions request containing subsystem identifier
     * @return users with actions
     * @since 1.1
     */
    List<SSOUserWithActions> getUsersWithActions(GetUsersWithActionsRequest request);

    void updateUserOtpType(UpdateUserOtpTypeRequest request);

    /**
     * Registers user and gives him requested principal.
     * User is created in incomplete state. In that state, if
     * a registration with the same username is made, existing user
     * is destroyed. To complete a user, call #completeUser() method.
     * Also, user is completed when he logs in.
     *
     * @param request register user request containing all the
     *                        data needed to register a user
     * @throws UserExistsException       if user with such a name already exists
     * @see RoleGrantSpecification
     * @see #completeUser(CompleteUserRequest)
     */
    void registerUser(UserRegisterRequest request)
            throws UserExistsException, PolicyValidationException,
            BadPublicKeyException, MessageSendException;

    /**
     * @param request change temp password request containing username and new password
     * @since 1.2
     */
    void changeTempPassword(ChangeTempPasswordRequest request) throws PolicyValidationException;

    /**
     * Returns a description of the given user.
     *
     * @param request get user description request containing username
     * @return description or null if user is not found
     * @since 1.2-4
     */
    UserDescription getUserDescription(GetUserDescriptionRequest request);

    /**
     * Reset Master Key
     * @param request reset Google Auth master key request containing username
     * @throws UserNotFoundException if no such user
     * @return New master key
     * @since 1.7
     */
    String resetGoogleAuthMasterKey(ResetGoogleAuthMasterKeyRequest request) throws UserNotFoundException, SsoDecryptException;

    /**
     * Get google auth QR code
     *
     * @param request get Google Auth QR code request containing secret key, issuer, and account name
     * @return an otpauth scheme URI for loading into a client application.
     */
    String getUrlToGoogleAuthQrCode(GetGoogleAuthQrCodeRequest request);

    void updateUserIsOtpOptionalValue(UpdateUserIsOtpOptionalValueRequest request);

    /**
     * Updates user's fields.
     *
     * @param request update user description request containing user description
     * @throws UserNotFoundException if user with such a name is not found
     * @throws BadPublicKeyException if public key is not valid
     * @since 1.2-4
     */
    void updateUserDescription(UpdateUserDescriptionRequest request) throws UserNotFoundException, BadPublicKeyException;

    /**
     * Resets user's password if policy requires this.
     *
     * @param reset password reset request containing reset description
     * @throws UserNotFoundException     if not such user exists
     * @throws PolicyValidationException if password is bad
     * @since 1.6-3
     */
    void resetPassword(PasswordResetRequest reset) throws UserNotFoundException, PolicyValidationException;

    /**
     * Finds users by list of their logins and returns their status information.
     *
     * @param request get user statuses request containing list of user names (logins)
     * @return user statuses
     * @since 1.3-5
     */
    List<UserStatus> getUserStatuses(GetUserStatusesRequest request);

    /**
     * Exchanges subsystem token to SSOUser. After this operation
     * returns, subsystem token is not valid anymore and cannot
     * be used for exchanging.
     *
     * @param request exchange subsystem token request containing subsystem token
     * @return SSOUser or null if token does not exist, expired or
     * already used
     * @since 1.4
     */
    SSOUser exchangeSubsystemToken(ExchangeSubsystemTokenRequest request);

    /**
     * Touches sessions: that is, updates their access time to avoid
     * removal. If a session was issued by an SSO session, the latter
     * is touched too.
     *
     * @param request touch sessions request containing IDs of sessions to touch
     */
    void touchSessions(TouchSessionsRequest request);

    /**
     * Makes a user complete. This protects a registered user
     * from being overwritten.
     *
     * @param request complete user request containing username
     * @see # registerUser(UserRegisterRequest)
     * @since 1.5
     */
    void completeUser(CompleteUserRequest request);

    /**
     * Revokes from a user all his roles and replaces them with a given role.
     *
     * @param request change user role request containing username, new role, and subsystem hint
     */
    void changeUserRole(ChangeUserRoleRequest request);
}
