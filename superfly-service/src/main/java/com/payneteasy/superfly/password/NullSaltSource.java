package com.payneteasy.superfly.password;

/**
 * Always supplies null for salt. This leads to unsalted passwords.
 *
 * <p>Not a bean: no policy uses unsalted passwords, both of them store a
 * per-user salt through {@link RandomStoredSaltSource}. Kept for tests.
 *
 * @author Roman Puchkovskiy
 */
public class NullSaltSource implements SaltSource {

    public String getSalt(String username) {
        return null;
    }

    public String getSalt(long userId) {
        return null;
    }

}
