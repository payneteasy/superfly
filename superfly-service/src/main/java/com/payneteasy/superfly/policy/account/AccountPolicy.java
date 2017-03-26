package com.payneteasy.superfly.policy.account;

import com.payneteasy.superfly.service.UserService;

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
     * @param userId                    ID of a user
     * @param unlockingSuspendedUser    whether suspended user is unlocked
     * @return non-null value if a temp password was generated (this value is
     * this password) or null if password is not changed
     */
    String unlockUser(long userId, boolean unlockingSuspendedUser);

    /**
     * Suspends users which were inactive for the given period of time if it's
     * needed.
     *
     * @param days            number of days
     * @param userService    user service
     */
    void suspendUsersIfNeeded(int days, UserService userService);

    /**
     * Expires password which are too old if it's needed.
     *
     * @param days            number of days (max age of password before expiration)
     * @param userService    user service
     */
    void expirePasswordsIfNeeded(int days, UserService userService);
}
