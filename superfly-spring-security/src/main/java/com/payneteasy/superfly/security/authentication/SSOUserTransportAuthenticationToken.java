package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.TwoStepAuthenticationProcessingFilter;

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
	private static final long serialVersionUID = -5726837452086192434L;

	public static final String SESSION_KEY = "superfly-sso-user-transport-token";
	
	private SSOUser ssoUser;

	public SSOUserTransportAuthenticationToken(SSOUser ssoUser) {
		this.ssoUser = ssoUser;
	}

	public SSOUser getSsoUser() {
		return ssoUser;
	}

	@Override
	public String getName() {
		return getSsoUser().getName();
	}

}
