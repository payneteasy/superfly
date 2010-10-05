package com.payneteasy.superfly.password;

/**
 * Supplies salt values. That salt is used by {@link PasswordEncoder} instances.
 * 
 * @author Roman Puchkovskiy
 * @see PasswordEncoder
 */
public interface SaltSource {
	/**
	 * Returns salt value.
	 * 
	 * @param username	username for which to get salt
	 * @return salt
	 */
	String getSalt(String username);
}
