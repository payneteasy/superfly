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
	@AStoredProcedure(name = "increment_hotp_counter")
	void incrementCounter(String username, long newValue);
}
