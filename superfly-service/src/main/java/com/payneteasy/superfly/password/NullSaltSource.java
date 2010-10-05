package com.payneteasy.superfly.password;

/**
 * Always supplies null for salt. This leads to unsalted passwords.
 * 
 * @author Roman Puchkovskiy
 */
public class NullSaltSource implements SaltSource {

	public String getSalt(String username) {
		return null;
	}

}
