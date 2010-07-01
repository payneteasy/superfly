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
	SSOUser authenticate(String username, String password,
			AuthenticationRequestInfo authRequestInfo);

	/**
	 * Sends data about this system to SSO server.
	 * 
	 * @param subsystemIdentifier
	 *            identifier of this system (it's just a hint for SSO server)
	 * @param actionDescriptions
	 *            descriptions of actions of this system
	 */
	void sendSystemData(String subsystemIdentifier,
			ActionDescription[] actionDescriptions);

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
    * Registers user and gives him requested principal.
    * 
    * @param username				name of the user
    * @param password				password of the user
    * @param email					email of the user
    * @param subsystemIdentifier	identifier of the subsystem from which
    * 								users will be obtained
    * @param principalNames 		principals which roles to give to users
    * @throws UserExistsException if user with such a name already exists
    * @since 1.1
    */
	void registerUser(String username, String password, String email,
			String subsystemIdentifier, String[] principalNames)
			throws UserExistsException;

}
