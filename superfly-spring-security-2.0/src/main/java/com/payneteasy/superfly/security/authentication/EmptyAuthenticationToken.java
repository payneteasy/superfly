package com.payneteasy.superfly.security.authentication;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

/**
 * Authentication implementation which actually does not auth to anything but
 * just used for something else (like value transport).
 * 
 * @author Roman Puchkovskiy
 */
public class EmptyAuthenticationToken implements Authentication {
	private static final long serialVersionUID = 5670753128479212097L;

	public GrantedAuthority[] getAuthorities() {
		return new GrantedAuthority[]{};
	}

	public Object getCredentials() {
		return null;
	}

	public Object getDetails() {
		return null;
	}

	public Object getPrincipal() {
		return null;
	}

	public boolean isAuthenticated() {
		return false;
	}

	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
	}

	public String getName() {
		return null;
	}

}
