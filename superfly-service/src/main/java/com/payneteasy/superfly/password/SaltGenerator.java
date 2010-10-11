package com.payneteasy.superfly.password;

/**
 * Generator for salts. It's intended to generate a new value on each call.
 *
 * @author Roman Puchkovskiy
 */
public interface SaltGenerator {
	/**
	 * Generates a random salt value.
	 * 
	 * @return salt value
	 */
	String generate();
}