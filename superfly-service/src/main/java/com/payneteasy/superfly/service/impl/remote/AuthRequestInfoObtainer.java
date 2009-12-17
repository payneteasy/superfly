package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;

/**
 * Obtainer which obtains subsystem identifier from AuthenticationRequestInfo.
 * For it to work, such info must be put to that object which is not mandatory.
 * 
 * @author Roman Puchkovskiy
 */
public class AuthRequestInfoObtainer implements SubsystemIdentifierObtainer {

	public String obtainSubsystemIdentifier(
			AuthenticationRequestInfo authRequestInfo) {
		return authRequestInfo.getSubsystemIdentifier();
	}

}
