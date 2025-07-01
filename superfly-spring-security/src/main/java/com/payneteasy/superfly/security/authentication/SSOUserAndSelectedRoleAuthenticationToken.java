package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.SuperflyAuthenticationProvider;
import lombok.Getter;

import java.io.Serial;

/**
 * Authentication implementation which is used as an authentication request to
 * SuperflyAuthenticationProvider on the final stage of authentication.
 *
 * @author Roman Puchkovskiy
 * @see SuperflyAuthenticationProvider
 */
@Getter
public class SSOUserAndSelectedRoleAuthenticationToken extends EmptyAuthenticationToken {
    @Serial
    private static final long serialVersionUID = -3043969728081312772L;

    private final SSOUser ssoUser;
    private final SSORole ssoRole;

    public SSOUserAndSelectedRoleAuthenticationToken(SSOUser ssoUser,
            SSORole ssoRole) {
        super();
        this.ssoUser = ssoUser;
        this.ssoRole = ssoRole;
    }

}
