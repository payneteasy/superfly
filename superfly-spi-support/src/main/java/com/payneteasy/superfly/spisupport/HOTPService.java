package com.payneteasy.superfly.spisupport;

import com.payneteasy.superfly.api.MessageSendException;

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
	 * @throws MessageSendException 
	 */
	void sendTableIfSupported(long userId) throws MessageSendException;

	/**
	 * Resets a table and sends a new table to a user if provider supports
	 * sending.
	 * 
	 * @param userId	ID of a user
	 * @throws MessageSendException 
	 */
	void resetTableAndSendIfSupported(long userId) throws MessageSendException;
}
