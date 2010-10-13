package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;

/**
 * Base for {@link AuthenticationProvider} which uses {@link SSOService} to
 * authenticate against Superfly.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSingleStepAuthenticationProvider extends AbstractRoleTransformingAuthenticationProvider {
	
	protected boolean finishWithSuperflyFinalAuthentication = false;
	protected SSOService ssoService;
	
	public void setFinishWithSuperflyFinalAuthentication(boolean b) {
		this.finishWithSuperflyFinalAuthentication = b;
	}
	
	@Required
	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	protected Authentication createFinalAuthentication(Authentication authentication,
			SSOUser ssoUser, SSORole role) {
		return new SSOUserAuthenticationToken(ssoUser, role,
				authentication.getCredentials(), authentication.getDetails(),
				roleNameTransformers, roleSource);
	}
	
	protected Authentication createAuthentication(Authentication authRequest,
			SSOUser ssoUser) {
		Authentication result;
		if (finishWithSuperflyFinalAuthentication && ssoUser.getActionsMap().size() == 1) {
			result = createFinalAuthentication(authRequest, ssoUser,
					ssoUser.getActionsMap().keySet().iterator().next());
		} else {
			result = createNonFinalAuthentication(authRequest, ssoUser);
		}
		return result;
	}
	
	protected abstract Authentication createNonFinalAuthentication(Authentication auth, SSOUser ssoUser);
}