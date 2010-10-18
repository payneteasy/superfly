package com.payneteasy.superfly.password;

/**
 * Dummy encoder which does not actually encode anything. Note, however, that
 * salt will be appended to the password if supplied.
 * 
 * @author Roman Puchkovskiy
 */
public class PlaintextPasswordEncoder extends AbstractPasswordEncoder {

	public String encode(String plainPassword, String salt) {
		return mergePasswordAndSalt(plainPassword, salt);
	}

}
