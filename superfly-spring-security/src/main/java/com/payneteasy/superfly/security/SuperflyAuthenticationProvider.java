package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Authentication provider which is able to authenticate against the following
 * pair: <SSOUser, SSORole>.
 *
 * @author Roman Puchkovskiy
 */
@Component
public class SuperflyAuthenticationProvider extends AbstractSuperflyAuthenticationProvider {

    private SSOService ssoService;

    @Autowired
    public void setSsoService(SSOService ssoService) {
        this.ssoService = ssoService;
    }

    @Override
    protected SSOUser doAuthenticate(
            UsernamePasswordAuthRequestInfoAuthenticationToken authRequest,
            String username,
            String password
    ) {
        return ssoService.authenticate(
                AuthenticateRequest
                        .builder()
                        .username(username)
                        .password(password)
                        .authRequestInfo(authRequest.getAuthRequestInfo())
                        .build()
        );
    }
}
