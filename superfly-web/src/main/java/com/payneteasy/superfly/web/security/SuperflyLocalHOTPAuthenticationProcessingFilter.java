package com.payneteasy.superfly.web.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.security.SuperflyHOTPAuthenticationProcessingFilter;

/**
 * Filter for HOTP checking in Superfly webapp itself.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflyLocalHOTPAuthenticationProcessingFilter extends
        SuperflyHOTPAuthenticationProcessingFilter {

    @Override
    protected Authentication createSimpleAuthRequest(
            Authentication authentication, String hotp) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        return new LocalCheckHOTPToken(authentication.getName(), hotp, token.getAuthorities());
    }

}
