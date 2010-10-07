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
public abstract class AbstractSuperflySingleStepAuthenticationProvider implements
		AuthenticationProvider {
	
	protected boolean finishWithSuperflyFinalAuthentication = false;
	private StringTransformer[] roleNameTransformers = new StringTransformer[]{};
	private RoleSource roleSource = createDefaultRoleSource();
	protected SSOService ssoService;
	
	public void setFinishWithSuperflyFinalAuthentication(boolean b) {
		this.finishWithSuperflyFinalAuthentication = b;
	}
	
	public void setRoleNameTransformers(StringTransformer[] roleNameTransformers) {
		this.roleNameTransformers = roleNameTransformers;
	}

	public void setRoleSource(RoleSource roleSource) {
		this.roleSource = roleSource;
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
	
	protected RoleSource createDefaultRoleSource() {
		RoleSource[] sources = new RoleSource[2];
		sources[0] = new SSOActionRoleSource();
		sources[1] = new SSORoleRoleSource();
		return new CompoundRoleSource(sources);
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