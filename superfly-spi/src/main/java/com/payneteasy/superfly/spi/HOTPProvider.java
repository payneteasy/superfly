package com.payneteasy.superfly.spi;

/**
 * Provides HOTP (HMAC-based One Time Password) facilities.
 * 
 * Roman Puchkovskiy
 */
public interface HOTPProvider {
	/**
	 * Authenticates a user using HOTP.
	 * 
	 * @param username	name of the user
	 * @param hotp		HOTP
	 * @return authentication result
	 */
	boolean authenticate(String username, String hotp);
}
