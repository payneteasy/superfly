package com.payneteasy.superfly.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;

/**
 * {@link AuthenticationProvider} which does not authenticate anything but
 * just creates {@link SSOUserAuthenticationToken} based on a role selected by
 * the user.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflySelectRoleAuthenticationProvider extends AbstractRoleTransformingAuthenticationProvider {

    public SuperflySelectRoleAuthenticationProvider() {
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        Authentication result = null;
        if (authentication instanceof SSOUserAndSelectedRoleAuthenticationToken) {
            SSOUserAndSelectedRoleAuthenticationToken token = (SSOUserAndSelectedRoleAuthenticationToken) authentication;
            return new SSOUserAuthenticationToken(token.getSsoUser(), token.getSsoRole(),
                    token.getCredentials(), token.getDetails(), roleNameTransformers, roleSource);
        }
        return result;
    }

    public boolean supports(Class<?> authentication) {
        return SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
