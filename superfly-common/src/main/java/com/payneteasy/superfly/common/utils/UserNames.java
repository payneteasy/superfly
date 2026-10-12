package com.payneteasy.superfly.common.utils;

/**
 * What a user name stored in Superfly can look like.
 */
public final class UserNames {

    /** Length of {@code users.user_name} and of the user name parameters of the stored procedures. */
    public static final int MAX_LENGTH = 32;

    private UserNames() {
    }

    /**
     * @param username name to check
     * @return true if a user with this name can exist: it is not empty and fits the database column
     */
    public static boolean isPossible(String username) {
        return username != null && !username.isEmpty() && username.length() <= MAX_LENGTH;
    }
}
