package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.AuthenticateRequest;
import com.payneteasy.superfly.api.request.CheckOtpRequest;
import com.payneteasy.superfly.common.utils.StringUtils;
import com.payneteasy.superfly.security.authentication.OtpUsernamePasswordCheckedToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * {@link AuthenticationProvider} which uses Superfly password authentication
 * to authenticate user.
 *
 * @author Roman Puchkovskiy
 */
@Slf4j
public class SuperflyUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    @Setter
    private SSOService ssoService;

    public SuperflyUsernamePasswordAuthenticationProvider() {
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (authentication instanceof UsernamePasswordAuthRequestInfoAuthenticationToken authRequest) {
            if (authRequest.getCredentials() == null) {
                throw new BadCredentialsException("Null credentials");
            }
            SSOUser ssoUser = ssoService.authenticate(new AuthenticateRequest(
                    authRequest.getName(),
                    authRequest.getCredentials().toString(),
                    authRequest.getAuthRequestInfo()
            ));
            if (ssoUser == null) {
                throw new BadCredentialsException("Bad credentials");
            }
            String ssoUserName = ssoUser.getName();
            if (log.isDebugEnabled()) {
                log.debug("Authenticated user: <{}>, with otp: <{} [optional: {}]>, actions size: <{}>",
                          ssoUserName,
                          ssoUser.getOtpType(),
                          ssoUser.isOtpOptional(),
                          ssoUser.getActionCount()
                );
                log.debug("User <{}> has second factory: <{}>", ssoUserName, authRequest.getSecondFactory());
            }
            if (ssoUser.getActionsMap().isEmpty()) {
                log.error("User <{}> has no roles", ssoUserName);
                throw new BadCredentialsException("No roles");
            }

            boolean otpIsRequired = !ssoUser.isOtpOptional();

            if (otpIsRequired && StringUtils.isEmpty(authRequest.getSecondFactory())) {
                log.debug("User <{}> has otp type <{}> and is not optional, but no second factory provided",
                          ssoUserName, ssoUser.getOtpType());
                return createOtpAuthentication(ssoUser);
            }

            if (StringUtils.hasText(authRequest.getSecondFactory())) {
                log.debug("User <{}> has otp type <{}> and is optional, second factory provided", ssoUserName, ssoUser.getOtpType());
                if (ssoService.checkOtp(new CheckOtpRequest(ssoUser, authRequest.getSecondFactory()))) {
                    return createAuthentication(ssoUser);
                } else {
                    log.debug("User <{}> has otp type <{}> and is optional, but no second factory provided",
                              ssoUserName, ssoUser.getOtpType());
                    throw new BadOTPValueException("Otp check failed!");
                }
            }

            return createAuthentication(ssoUser);
        }
        return null;
    }

    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication);
    }

    protected Authentication createAuthentication(SSOUser ssoUser) {
        return new UsernamePasswordCheckedToken(ssoUser);
    }

    protected Authentication createOtpAuthentication(SSOUser ssoUser) {
        return new OtpUsernamePasswordCheckedToken(ssoUser);
    }

}
