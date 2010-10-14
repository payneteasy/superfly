package com.payneteasy.superfly.service;

/**
 * Service used for local security-related purposes.
 * 
 * @author Roman Puchkovskiy
 */
public interface LocalSecurityService {
	/**
	 * Authenticates a local user.
	 * 
	 * @param username	user name
	 * @param password	password
	 * @return actions assigned to the given user on success, or null on
	 * failure (failure occurs if no such user exists, password does not match,
	 * or if user is locked)
	 */
	String[] authenticate(String username, String password);
	
	/**
	 * Authenticates a user's one-time password.
	 * 
	 * @param username	user name
	 * @param hotp		one-time password
	 * @return authentication result
	 */
	boolean authenticateUsingHOTP(String username, String hotp);
}
