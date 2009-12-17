package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;

/**
 * Used to somehow obtain a subsystem identifier
 * 
 * @author Roman Puchkovskiy
 */
public interface SubsystemIdentifierObtainer {
	/**
	 * Obtains a subsystem identifier.
	 * 
	 * @param authRequestInfo	info used to request authentication
	 * @return subsystem identifier
	 */
	String obtainSubsystemIdentifier(AuthenticationRequestInfo authRequestInfo);
}
