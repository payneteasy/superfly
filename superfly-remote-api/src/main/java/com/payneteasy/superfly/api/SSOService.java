package com.payneteasy.superfly.api;

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
	 * @param username
	 *            username to use when authenticating
	 * @param password
	 *            password to use when authenticating
	 * @param authRequestInfo
	 *            additional info about authentication request
	 * @return user object on success or null when authentication failed (for
	 *         instance, no such user, or password mismatched, or user is
	 *         blocked...)
	 */
	SSOUser authenticate(String username, String password, AuthenticationRequestInfo authRequestInfo);

	/**
	 * Sends data about this system to SSO server.
	 * 
	 * @param subsystemIdentifier
	 *            identifier of this system (it's just a hint for SSO server)
	 * @param actionDescriptions
	 *            descriptions of actions of this system
	 */
	void sendSystemData(String subsystemIdentifier, ActionDescription[] actionDescriptions);

	/**
	 * Returns a list of users with their actions granted through role with the
	 * given principal.
	 * 
	 * @param subsystemIdentifier
	 *            identifier of the subsystem from which users will be obtained
	 * @return users with actions
	 * @since 1.1
	 */
	List<SSOUserWithActions> getUsersWithActions(String subsystemIdentifier);

	/**
	 * Authenticates a user using an HOTP implementation configured on the
	 * Superfly server.
	 * 
	 * @param username	name of the user to authenticate
	 * @param hotp		one-time password
	 * @return true if authentication is successful
	 * @since 1.2
	 */
	boolean authenticateUsingHOTP(String username, String hotp);

	/**
	 * Registers user and gives him requested principal.
	 * 
	 * @param username
	 *            name of the user
	 * @param password
	 *            password of the user
	 * @param email
	 *            email of the user
	 * @param subsystemHint
	 *            hint to the identifier of a subsystem from which roles will be
	 *            taken
	 * @param roleGrants
	 *            which roles to grant
	 * @throws UserExistsException
	 *             if user with such a name already exists
	 * @since 1.1
	 * @see RoleGrantSpecification
	 */
	void registerUser(String username, String password, String email, String subsystemHint,
			RoleGrantSpecification[] roleGrants, String name, String surname, String secretQuestion, String secretAnswer)
			throws UserExistsException,PolicyValidationException;
	/**
     * 
     * @param userName user name
     * @return flag of temporary password
     */
    String getFlagTempPassword(String userName);
    
    /**
     * 
     * @param userName user name
     * @param password password
     */
    void changeTempPassword(String userName, String password);
}
