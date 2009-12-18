package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.AuthenticationProvider;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;

/**
 * Authentication provider which is able to authenticate against the following
 * pair: <SSOUser, SSORole>.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyAuthenticationProvider implements AuthenticationProvider {
	
	private SSOService ssoService;
	private StringTransformer[] transformers = new StringTransformer[0];
	
	@Required
	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	public void setTransformers(StringTransformer[] transformers) {
		this.transformers = transformers;
	}

	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		if (authentication instanceof UsernamePasswordAuthRequestInfoAuthenticationToken) {
			// step 1: auth username/password to temp Authentication
			UsernamePasswordAuthRequestInfoAuthenticationToken authRequest = (UsernamePasswordAuthRequestInfoAuthenticationToken) authentication;
			return authenticateUsernamePassword(authRequest);
		} else if (authentication instanceof SSOUserTransportAuthenticationToken) {
			// step 1.5: throwing so ExceptionTranslationFilter will catch it and
			// give entry point a chance to go to step 2
			throw new StepTwoException("Going to step two");
		} else if (authentication instanceof SSOUserAndSelectedRoleAuthenticationToken) {
			// step 2: auth user+role to final Authentication
			SSOUserAndSelectedRoleAuthenticationToken authRequest = (SSOUserAndSelectedRoleAuthenticationToken) authentication;
			return authenticateUserRole(authRequest);
		} else {
			// this is not our responsibility...
			return null;
		}
	}

	protected Authentication authenticateUserRole(
			SSOUserAndSelectedRoleAuthenticationToken authRequest) {
		SSOUser user = authRequest.getSsoUser();
		SSORole role = authRequest.getSsoRole();

		return createFinalAuthentication(authRequest, user, role);
	}

	protected Authentication authenticateUsernamePassword(
			UsernamePasswordAuthRequestInfoAuthenticationToken authRequest) {
		String username = authRequest.getName();
		String password = (String) authRequest.getCredentials();
		
		SSOUser ssoUser = ssoService.authenticate(username, password,
				authRequest.getAuthRequestInfo());
		
		if (ssoUser == null) {
			throw new BadCredentialsException("Bad password");
		}
		
		if (ssoUser.getActionsMap().isEmpty()) {
			throw new BadCredentialsException("No roles assigned");
		}
		
		if (ssoUser.getActionsMap().keySet().size() > 1) {
			// more than 1 role, we need step two
			return createTempAuthenticationToken(ssoUser);
		} else {
			// exactly one role, no step two is needed, short-circuit
			return createFinalAuthentication(authRequest, ssoUser,
					ssoUser.getActionsMap().keySet().iterator().next());
		}
	}

	protected SSOUserTransportAuthenticationToken createTempAuthenticationToken(
			SSOUser ssoUser) {
		return new SSOUserTransportAuthenticationToken(ssoUser);
	}

	protected Authentication createFinalAuthentication(
			Authentication authRequest, SSOUser user, SSORole role) {
		return new SSOUserAuthenticationToken(user, role,
				authRequest.getCredentials(), authRequest.getDetails(),
				transformers);
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class authentication) {
		return UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication)
				|| SSOUserTransportAuthenticationToken.class.isAssignableFrom(authentication)
				|| SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
