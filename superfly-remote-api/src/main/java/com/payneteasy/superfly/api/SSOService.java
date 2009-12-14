package com.payneteasy.superfly.api;

/**
 * Service for SSO communication.
 * 
 * @author Roman Puchkovskiy
 */
public interface SSOService {
	/**
	 * Attempts to authenticate user from the current system using the supplied
	 * username and password. If successful, returns user object, else returns
	 * null.
	 * 
	 * @param username	username to use when authenticating
	 * @param password	password to use when authenticating
	 * @return user object on success or null when authentication failed (for
	 * instance, no such user, or password mismatched, or user is blocked...)
	 */
	SSOUser authenticate(String username, String password);
}
