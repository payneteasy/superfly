package com.payneteasy.superfly.security;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

/**
 * Authentication provider which is able to authenticate against the following
 * pair: <SSOUser, SSORole>.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyAuthenticationProvider extends AbstractSuperflyAuthenticationProvider {

    private SSOService ssoService;

    @Required
    public void setSsoService(SSOService ssoService) {
        this.ssoService = ssoService;
    }

    @Override
    protected SSOUser doAuthenticate(
            UsernamePasswordAuthRequestInfoAuthenticationToken authRequest,
            String username, String password) {
        SSOUser ssoUser = ssoService.authenticate(username, password,
                authRequest.getAuthRequestInfo());
        return ssoUser;
    }
}
