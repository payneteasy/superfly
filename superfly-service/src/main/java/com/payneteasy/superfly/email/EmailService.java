package com.payneteasy.superfly.email;

/**
 * Used to send emails.
 *
 * @author Roman Puchkovskiy
 */
public interface EmailService {
	/**
	 * Sends a message containing an HOTP table.
	 * 
	 * @param to		to address
	 * @param fileName	file name
	 * @param table		table content
	 * @throws RuntimeMessagingException
	 */
	void sendHOTPTable(String to, String fileName, byte[] table) throws RuntimeMessagingException;

	/**
	 * Sends a message which complains that there's no public key for the user.
	 * 
	 * @param email		to address
	 * @throws RuntimeMessagingException
	 */
	void sendNoPublicKeyMessage(String email) throws RuntimeMessagingException;
}
