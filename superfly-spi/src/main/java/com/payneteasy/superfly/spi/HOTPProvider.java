package com.payneteasy.superfly.spi;

import com.payneteasy.superfly.spisupport.ObjectResolver;

/**
 * Provides HOTP (HMAC-based One Time Password) facilities.
 * 
 * @author Roman Puchkovskiy
 * @since 1.2
 */
public interface HOTPProvider {

	/**
	 * Initializes the provider.
	 * 
	 * @param objectResolver	resolver which may be used to provide dependencies
	 */
	void init(ObjectResolver objectResolver);
	
	/**
	 * Authenticates a user using HOTP.
	 * 
	 * @param username	name of the user
	 * @param hotp		HOTP
	 * @return authentication result
	 */
	boolean authenticate(String username, String hotp);
}
