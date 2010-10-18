package com.payneteasy.superfly.security.validator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Validates authentication.
 *
 * @author Roman Puchkovskiy
 */
public interface AuthenticationValidator {
	/**
	 * Carries out authentication.
	 * 
	 * @param auth	auth to validate
	 * @throws AuthenticationException	if validation fails
	 */
	void validate(Authentication auth) throws AuthenticationException;
}
