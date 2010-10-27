package com.payneteasy.superfly.hotp;

/**
 * Service to deal with HOTP management.
 *
 * @author Roman Puchkovskiy
 */
public interface HOTPService {
	/**
	 * Sends a table to a user if provider supports this.
	 * 
	 * @param userId	ID of a user
	 */
	void sendTableIfSupported(long userId);
}
