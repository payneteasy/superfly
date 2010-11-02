package com.payneteasy.superfly.spisupport;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;

/**
 * DAO to work with HOTP algorithm.
 *
 * @author Roman Puchkovskiy
 */
public interface HOTPDao {
	/**
	 * Returns HOTP data for the given user.
	 * 
	 * @param username	name of the user
	 * @return HOTP data
	 */
	@AStoredProcedure(name = "get_hotp_data")
	HOTPData getHOTPData(String username);
	
	/**
	 * Increments an HOTP value for the given user.
	 * 
	 * @param username	name of the user
	 * @param newValue	value to set for the counter
	 */
	@AStoredProcedure(name = "update_hotp_counter")
	void updateCounter(String username, long newValue);
	
	/**
	 * Resets HOTP sequence for the given user.
	 * 
	 * @param username	name of the user
	 * @param hotpSalt	new salt for HOTP
	 */
    @AStoredProcedure(name="reset_hotp")
    void resetHOTP(String username, String hotpSalt);
}
