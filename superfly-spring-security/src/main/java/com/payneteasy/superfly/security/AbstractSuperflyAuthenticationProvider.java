package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;

/**
 * Base for authentication providers using a two-step password authentication
 * for Superfly.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSuperflyAuthenticationProvider implements AuthenticationProvider {

    private boolean enabled = true;
    private StringTransformer[] transformers = new StringTransformer[0];
    private RoleSource roleSource = createDefaultRoleSource();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected boolean isActive() {
        return enabled;
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class authentication) {
        return UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication)
                || SSOUserTransportAuthenticationToken.class.isAssignableFrom(authentication)
                || SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setTransformers(StringTransformer[] transformers) {
        this.transformers = transformers;
    }

    public void setRoleSource(RoleSource roleSource) {
        this.roleSource = roleSource;
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (isActive()) {
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
        } else {
            return null;
        }
    }

    protected Authentication authenticateUserRole(SSOUserAndSelectedRoleAuthenticationToken authRequest) {
        SSOUser user = authRequest.getSsoUser();
        SSORole role = authRequest.getSsoRole();

        return createFinalAuthentication(authRequest, user, role);
    }

    protected Authentication authenticateUsernamePassword(UsernamePasswordAuthRequestInfoAuthenticationToken authRequest) {
        String username = authRequest.getName();
        String password = (String) authRequest.getCredentials();

        SSOUser ssoUser = doAuthenticate(authRequest, username, password);

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

    protected SSOUserTransportAuthenticationToken createTempAuthenticationToken(SSOUser ssoUser) {
        return new SSOUserTransportAuthenticationToken(ssoUser);
    }

    protected Authentication createFinalAuthentication(Authentication authRequest, SSOUser user,
            SSORole role) {
                return new SSOUserAuthenticationToken(user, role,
                        authRequest.getCredentials(), authRequest.getDetails(),
                        transformers, roleSource);
            }

    protected RoleSource createDefaultRoleSource() {
        RoleSource[] sources = new RoleSource[2];
        sources[0] = new SSOActionRoleSource();
        sources[1] = new SSORoleRoleSource();
        return new CompoundRoleSource(sources);
    }

    protected abstract SSOUser doAuthenticate(UsernamePasswordAuthRequestInfoAuthenticationToken authRequest, String username,
            String password);

}