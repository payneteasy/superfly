package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.SuperflyAuthenticationProvider;

/**
 * Authentication implementation which is used as an authentication request to
 * SuperflyAuthenticationProvider on the final stage of authentication.
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
