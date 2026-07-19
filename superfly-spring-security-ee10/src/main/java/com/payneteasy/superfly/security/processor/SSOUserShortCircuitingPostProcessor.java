package com.payneteasy.superfly.security.processor;

import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.CompoundRoleSource;
import com.payneteasy.superfly.security.RoleSource;
import com.payneteasy.superfly.security.SSOActionRoleSource;
import com.payneteasy.superfly.security.SSORoleRoleSource;
import com.payneteasy.superfly.security.StringTransformer;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

/**
 * Post-processor which allows to build a final authentication.
 *
 * @author Roman Puchkovskiy
 */
public class SSOUserShortCircuitingPostProcessor implements AuthenticationPostProcessor {

    private boolean finishWithSuperflyFinalAuthentication = false;
    private StringTransformer[] roleNameTransformers = new StringTransformer[]{};
    private RoleSource roleSource = createDefaultRoleSource();

    public void setFinishWithSuperflyFinalAuthentication(boolean b) {
        this.finishWithSuperflyFinalAuthentication = b;
    }

    public void setRoleNameTransformers(StringTransformer[] roleNameTransformers) {
        this.roleNameTransformers = roleNameTransformers;
    }

    public void setRoleSource(RoleSource roleSource) {
        this.roleSource = roleSource;
    }

    public Authentication postProcess(Authentication auth) {
        CompoundAuthentication compound = (CompoundAuthentication) auth;
        SSOUserTransportAuthenticationToken token = (SSOUserTransportAuthenticationToken) compound.getLatestReadyAuthentication();
        SSOUser ssoUser = token.getSsoUser();

        Authentication result;
        if (finishWithSuperflyFinalAuthentication && ssoUser.getActionsMap().size() == 1) {
            result = createFinalAuthentication(token, ssoUser,
                    ssoUser.getActionsMap().keySet().iterator().next());
        } else {
            result = auth;
        }
        return result;
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

}
