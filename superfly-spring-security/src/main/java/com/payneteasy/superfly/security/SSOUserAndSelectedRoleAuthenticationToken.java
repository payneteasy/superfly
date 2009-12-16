package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

/**
 * Authentication implementation which is used as an authentication request to
 * SuperflyAuthenticationProvider.
 * 
 * @author Roman Puchkovskiy
 * @see SuperflyAuthenticationProvider
 */
public class SSOUserAndSelectedRoleAuthenticationToken extends EmptyAuthenticationToken {

	private SSOUser ssoUser;
	private SSORole ssoRole;

	public SSOUserAndSelectedRoleAuthenticationToken(SSOUser ssoUser,
			SSORole ssoRole) {
		super();
		this.ssoUser = ssoUser;
		this.ssoRole = ssoRole;
	}
	
	public SSOUser getSsoUser() {
		return ssoUser;
	}

	public SSORole getSsoRole() {
		return ssoRole;
	}

}
