package com.payneteasy.superfly.security.processor;

import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;

/**
 * Post-processor which replaces a {@link CompoundAuthentication} with its
 * latest ready {@link Authentication}.
 *
 * @author Roman Puchkovskiy
 */
public class CompoundLatestAuthUnwrappingPostProcessor implements
		AuthenticationPostProcessor {

	public Authentication postProcess(Authentication auth) {
		CompoundAuthentication compound = (CompoundAuthentication) auth;
		return compound.getLatestReadyAuthentication();
	}

}
