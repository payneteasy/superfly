package com.payneteasy.superfly.policy.account;

/**
 * Defines policies relating to accounts.
 *
 * @author Roman Puchkovskiy
 */
public interface AccountPolicy {
	/**
	 * Unlocks a user. It may behave differently depending on unlockingSuspendedUser
	 * flag.
	 * 
	 * @param userId					ID of a user
	 * @param unlockingSuspendedUser	whether suspended user is unlocked
	 * @return non-null value if a temp password was generated (this value is
	 * this password) or null if password is not changed
	 */
	String unlockUser(long userId, boolean unlockingSuspendedUser);
}
