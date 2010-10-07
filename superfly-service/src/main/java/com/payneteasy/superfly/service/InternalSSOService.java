package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.RoleGrantSpecification;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.api.UserExistsException;
import com.payneteasy.superfly.api.PolicyValidationException;

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
	 * @throws UserExistsException
	 */
	void registerUser(String username, String password, String email, String subsystemIdentifier,
			RoleGrantSpecification[] roleGrants, String name, String surname, String secretQuestion, String secretAnswer)
			throws UserExistsException, PolicyValidationException;
}
