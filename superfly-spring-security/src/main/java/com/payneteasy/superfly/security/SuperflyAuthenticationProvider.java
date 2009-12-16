package com.payneteasy.superfly.security;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

/**
 * Authentication provider which is able to authenticate against the following
 * pair: <SSOUser, SSORole>.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyAuthenticationProvider implements AuthenticationProvider {
	
	private StringTransformer[] transformers = new StringTransformer[0];
	
	public void setTransformers(StringTransformer[] transformers) {
		this.transformers = transformers;
	}

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		SSOUserAndSelectedRoleAuthenticationToken authRequest = (SSOUserAndSelectedRoleAuthenticationToken) authentication;
		
		SSOUser user = authRequest.getSsoUser();
		SSORole role = authRequest.getSsoRole();

		SSOUserAuthenticationToken result = new SSOUserAuthenticationToken(user,
				role, authRequest.getCredentials(), authRequest.getDetails(),
				transformers);

        return result;
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class authentication) {
		return SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
