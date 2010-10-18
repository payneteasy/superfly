package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Base for {@link AuthenticationProvider} which knows how to deal with
 * roles.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractRoleTransformingAuthenticationProvider implements
		AuthenticationProvider {

	protected StringTransformer[] roleNameTransformers = new StringTransformer[]{};
	protected RoleSource roleSource = createDefaultRoleSource();

	public void setRoleNameTransformers(StringTransformer[] roleNameTransformers) {
		this.roleNameTransformers = roleNameTransformers;
	}

	public void setRoleSource(RoleSource roleSource) {
		this.roleSource = roleSource;
	}

	protected RoleSource createDefaultRoleSource() {
		RoleSource[] sources = new RoleSource[2];
		sources[0] = new SSOActionRoleSource();
		sources[1] = new SSORoleRoleSource();
		return new CompoundRoleSource(sources);
	}

}