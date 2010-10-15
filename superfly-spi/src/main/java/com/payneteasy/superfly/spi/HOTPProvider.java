package com.payneteasy.superfly.spi;

import java.io.IOException;
import java.io.OutputStream;

import com.payneteasy.superfly.spisupport.HOTPProviderContext;

/**
 * Provides HOTP (HMAC-based One Time Password) facilities.
 * 
 * @author Roman Puchkovskiy
 * @since 1.2
 */
public interface HOTPProvider {

	/**
	 * Initializes the provider.
	 * 
	 * @param context		context used to get parameters and dependencies
	 */
	void init(HOTPProviderContext context);
	
	/**
	 * Authenticates a user using HOTP.
	 * 
	 * @param username	name of the user
	 * @param hotp		HOTP
	 * @return authentication result
	 */
	boolean authenticate(String username, String hotp);
	
	/**
	 * Computes a HOTP value for a user with the given name.
	 * 
	 * @param username	name of the user
	 * @param counter	counter value
	 * @return HOTP value
	 */
	String computeValue(String username, long counter);

	/**
	 * Returns true if this provider can output sequence of HOTP values as
	 * a file for download via UI.
	 * 
	 * @return whether sequence can be downloaded
	 */
	boolean outputsSequenceForDownload();
	
	/**
	 * Writes a sequence for download to {@link OutputStream}.
	 * 
	 * @param username	name of the user
	 * @param os		output stream
	 * @throws IOException 
	 */
	void outputSequenceForDownload(String username, OutputStream os) throws IOException;
	
	/**
	 * Returns a name of file in which sequence is downloaded.
	 * 
	 * @param name of the user
	 * @return file name
	 */
	String getSequenceForDownloadFileName(String username);
}
