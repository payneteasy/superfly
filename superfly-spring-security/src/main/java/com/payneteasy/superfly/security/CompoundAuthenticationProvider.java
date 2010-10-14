package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.validator.AuthenticationValidator;

/**
 * {@link AuthenticationProvider} which allows to work with compound
 * (multi-step) authentication process. It delegates actual step authentication
 * to a delegate {@link AuthenticationProvider}.
 *
 * @author Roman Puchkovskiy
 */
public class CompoundAuthenticationProvider implements AuthenticationProvider {
	
	private AuthenticationProvider delegateProvider;
	private Class<?>[] supportedSimpleAuthenticationClasses = new Class<?>[]{};
	private AuthenticationValidator authenticationValidator = null;

	@Required
	public void setDelegateProvider(AuthenticationProvider authenticationProvider) {
		this.delegateProvider = authenticationProvider;
	}
	
	public void setSupportedSimpleAuthenticationClasses(Class<?>[] classes) {
		this.supportedSimpleAuthenticationClasses = classes;
	}

	public void setAuthenticationValidator(
			AuthenticationValidator authenticationValidator) {
		this.authenticationValidator = authenticationValidator;
	}

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (authenticationValidator != null) {
			authenticationValidator.validate(authentication);
		}
		CompoundAuthentication compoundAuthentication = null;
		Authentication request;
		if (authentication instanceof CompoundAuthentication) {
			compoundAuthentication = (CompoundAuthentication) authentication;
			request = compoundAuthentication.getCurrentAuthenticationRequest();
		} else {
			request = authentication;
		}
		Authentication result = delegateProvider.authenticate(request);
		if (compoundAuthentication == null) {
			compoundAuthentication = new CompoundAuthentication();
		}
		compoundAuthentication.addReadyAuthentication(result);
		return compoundAuthentication;
	}

	public boolean supports(Class<? extends Object> authentication) {
		if (CompoundAuthentication.class.isAssignableFrom(authentication)) {
			return true;
		}
		for (Class<?> clazz : supportedSimpleAuthenticationClasses) {
			if (clazz.equals(authentication)) {
				return true;
			}
		}
		return false;
	}

}
