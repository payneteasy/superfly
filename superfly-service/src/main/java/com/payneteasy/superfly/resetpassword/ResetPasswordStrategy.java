package com.payneteasy.superfly.resetpassword;

/**
 * Strategy for resetting a user's password.
 */
public interface ResetPasswordStrategy {
	/**
	 * Resets password.
	 * 
	 * @param userId		ID of the user
	 * @param userName		username
	 * @param password		new password
	 */
	void resetPassword(long userId, String userName, String password);
}
