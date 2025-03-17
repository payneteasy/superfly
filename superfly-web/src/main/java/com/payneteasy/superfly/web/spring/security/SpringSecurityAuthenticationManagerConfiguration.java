package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.security.CompoundAuthenticationProvider;
import com.payneteasy.superfly.security.processor.AuthenticationPostProcessor;
import com.payneteasy.superfly.security.processor.CompoundLatestAuthUnwrappingPostProcessor;
import com.payneteasy.superfly.security.validator.CompoundAuthenticationValidator;
import com.payneteasy.superfly.security.x509.X509PreAuthenticatedAuthenticationProvider;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.web.security.*;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

@Configuration
@AllArgsConstructor
public class SpringSecurityAuthenticationManagerConfiguration {
    private final SuperflyProperties   properties;
    private final LocalSecurityService localSecurityService;
    private final UserDetailsService   userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager() {
        List<AuthenticationProvider> providers = List.of(
                passwordAuthenticationProvider(),
                otpAuthenticationProvider(),
                x509PreAuthenticatedAuthenticationProvider()

        );
        return new ProviderManager(providers);
    }

    private AuthenticationProvider passwordAuthenticationProvider() {
        return new CompoundAuthenticationProvider()
                .setDelegateProvider(new SuperflyLocalAuthenticationProvider(localSecurityService))
                .setSupportedSimpleAuthenticationClasses(new Class[]{UsernamePasswordAuthenticationToken.class})
                .setNotSupportedSimpleAuthenticationClasses(new Class[]{LocalCheckOTPToken.class})
                .setAuthenticationPostProcessor(checkRequiredInitOtpPostProcessor());
    }

    private AuthenticationPostProcessor checkRequiredInitOtpPostProcessor() {
        return new CheckRequiredInitOtpAuthenticationPostProcessor()
                .setLocalSecurityService(localSecurityService)
                .setForceMultiFactorAuthMethod(properties.forceMultiFactorAuthMethod())
                .setEnableMultiFactorAuth(properties.enableMultiFactorAuth());
    }

    private AuthenticationProvider otpAuthenticationProvider() {
        CompoundAuthenticationValidator authenticationValidator = new CompoundAuthenticationValidator()
                .setRequiredClasses(new Class[]{UsernamePasswordAuthenticationToken.class});

        return new CompoundAuthenticationProvider()
                .setDelegateProvider(new SuperflyLocalOTPAuthenticationProvider(localSecurityService))
                .setAuthenticationValidator(authenticationValidator)
                .setAuthenticationPostProcessor(new CompoundLatestAuthUnwrappingPostProcessor());
    }


    private AuthenticationProvider x509PreAuthenticatedAuthenticationProvider() {
        return new X509PreAuthenticatedAuthenticationProvider(userDetailsService);
    }
}
