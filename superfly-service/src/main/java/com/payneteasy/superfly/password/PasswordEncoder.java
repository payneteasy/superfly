package com.payneteasy.superfly.password;

/**
 * Interface for encoding a password. Salt may be used if supplied. If salt is
 * null or empty it's ignored.
 * 
 * @author Roman Puchkovskiy
 */
public interface PasswordEncoder {
	/**
	 * Encodes a password using supplied salt. If salt is null or empty (""),
	 * it's ignored.
	 * 
	 * @param plainPassword		password to encode
	 * @param salt				salt
	 * @return encoded password
	 */
	String encode(String plainPassword, String salt);
}