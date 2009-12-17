package com.payneteasy.superfly.service;

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
}
