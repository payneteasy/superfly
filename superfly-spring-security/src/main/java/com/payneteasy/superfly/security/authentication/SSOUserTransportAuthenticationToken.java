package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.TwoStepAuthenticationProcessingFilter;
import lombok.Getter;

import java.io.Serial;

/**
 * Authentication implementation which is actually used to transport SSOUser
 * only.
 * This is used to store SSOUser temporarily in a SecurityContext between
 * steps of two-step authentication process.
 *
 * @author Roman Puchkovskiy
 * @see TwoStepAuthenticationProcessingFilter
 */
@Getter
public class SSOUserTransportAuthenticationToken extends EmptyAuthenticationToken {
    @Serial
    private static final long serialVersionUID = -5726837452086192434L;

    public static final String SESSION_KEY = "superfly-sso-user-transport-token";

    private final SSOUser ssoUser;

    public SSOUserTransportAuthenticationToken(SSOUser ssoUser) {
        this.ssoUser = ssoUser;
    }

    @Override
    public String getName() {
        return getSsoUser().getName();
    }

}
