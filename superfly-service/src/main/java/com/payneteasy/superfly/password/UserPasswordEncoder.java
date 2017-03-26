package com.payneteasy.superfly.password;

/**
 * Password encoding facade which hides how salt is obtained for a user.
 *
 * @author Roman Puchkovskiy
 */
public interface UserPasswordEncoder {
    /**
     * Encodes a password for the given user.
     *
     * @param plaintextPassword    password to encode
     * @param username            name of the user
     * @return encoded password
     */
    String encode(String plaintextPassword, String username);

    /**
     * Encodes a password for the given user.
     *
     * @param plaintextPassword    password to encode
     * @param userId            ID of the user
     * @return encoded password
     */
    String encode(String plaintextPassword, long userId);
}
