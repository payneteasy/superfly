package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOUser;

/**
 * Authentication implementation which is actually used to transport SSOUser
 * only.
 * This is used to store SSOUser temporarily in a SecurityContext between
 * steps of two-step authentication process.
 * 
 * @author Roman Puchkovskiy
 * @see TwoStepAuthenticationProcessingFilter
 */
public class SSOUserTransportAuthenticationToken extends EmptyAuthenticationToken {
	
	private SSOUser ssoUser;

	public SSOUserTransportAuthenticationToken(SSOUser ssoUser) {
		this.ssoUser = ssoUser;
	}

	public SSOUser getSsoUser() {
		return ssoUser;
	}

}
