package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Base for {@link AuthenticationProvider} which may be disabled.
 *
 * @author Roman Puchkovskiy
 */
public abstract class AbstractDisableableAuthenticationProvider implements AuthenticationProvider {
	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public final Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (!isEnabled()) {
			return null;
		}
		return doAuthenticate(authentication);
	}

	public final boolean supports(Class<? extends Object> authentication) {
		if (!isEnabled()) {
			return false;
		}
		return doSupports(authentication);
	}
	
	protected abstract Authentication doAuthenticate(Authentication authentication);
	
	protected abstract boolean doSupports(Class<? extends Object> authentication);

}
