package com.payneteasy.superfly.service;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.RoleDescription;
import com.payneteasy.superfly.api.SSOUser;

/**
 * Internal service used to implement SSOService.
 * 
 * @author Roman Puchkovskiy
 */
public interface InternalSSOService {
	/**
	 * Authenticates a user.
	 * 
	 * @param username				user name
	 * @param password				user password
	 * @param subsystemIdentifier	identifier of a subsystem from which user
	 * tries to log in
	 * @param userIpAddress			ID address of a user who tries to log in
	 * @param sessionInfo			session info
	 * @return SSOUser instance on success or null on failure
	 */
	SSOUser authenticate(String username, String password,
			String subsystemIdentifier, String userIpAddress,
			String sessionInfo);

	/**
	 * Saves system data.
	 * 
	 * @param subsystemIdentifier	identifier of the system
	 * @param roleDescriptions		descriptions of roles
	 * @param actionDescriptions	descriptions of actions
	 */
	void saveSystemData(String subsystemIdentifier,
			RoleDescription[] roleDescriptions,
			ActionDescription[] actionDescriptions);
}
