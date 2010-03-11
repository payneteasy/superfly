package com.payneteasy.superfly.common.password;

/**
 * Knows how to encrypt a password and how to check that the provided plain-text
 * password matches an encrypted version.
 * 
 * @author Roman Puchkovskiy
 */
public interface PasswordHandler {
	/**
	 * Encrypts a password.
	 * 
	 * @param password	password to encrypt
	 * @return encrypted password
	 */
	String encryptPassword(String password);
	/**
	 * Checks that a plain-text corresponds to its encrypted version.
	 * 
	 * @param passwordToCheck	plain-text password
	 * @param encryptedPassword	encrypted password to check against
	 * @return true if matches
	 */
	boolean checkPassword(String passwordToCheck, String encryptedPassword);
}
