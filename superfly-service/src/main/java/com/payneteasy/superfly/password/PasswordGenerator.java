package com.payneteasy.superfly.password;

/**
 * Generates random passwords.
 *
 * @author Roman Puchkovskiy
 */
public interface PasswordGenerator {

	/**
	 * Generates a password
	 * 
	 * @return password
	 */
	String generate();

}