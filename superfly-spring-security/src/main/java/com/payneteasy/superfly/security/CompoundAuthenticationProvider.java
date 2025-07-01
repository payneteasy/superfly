package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.processor.AuthenticationPostProcessor;
import com.payneteasy.superfly.security.validator.AuthenticationValidator;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * {@link AuthenticationProvider} which allows to work with compound
 * (multistep) authentication process. It delegates actual step authentication
 * to a delegate {@link AuthenticationProvider}.
 *
 * @author Roman Puchkovskiy
 */
@Component
@Setter
@Accessors(chain = true)
public class CompoundAuthenticationProvider extends AbstractDisableableAuthenticationProvider {

    private AuthenticationProvider      delegateProvider;
    private Class<?>[]                  supportedSimpleAuthenticationClasses    = new Class<?>[]{};
    private Class<?>[]                  notSupportedSimpleAuthenticationClasses = new Class<?>[]{};
    private AuthenticationValidator     authenticationValidator                 = null;
    private AuthenticationPostProcessor authenticationPostProcessor             = null;

    public CompoundAuthenticationProvider setDelegateProvider(AuthenticationProvider authenticationProvider) {
        this.delegateProvider = authenticationProvider;
        return this;
    }

    @Override
    protected Authentication doAuthenticate(Authentication authentication)
            throws AuthenticationException {
        if (authenticationValidator != null) {
            authenticationValidator.validate(authentication);
        }
        CompoundAuthentication compoundAuthentication = null;
        Authentication         request;
        if (authentication instanceof CompoundAuthentication) {
            compoundAuthentication = (CompoundAuthentication) authentication;
            request = compoundAuthentication.getCurrentAuthenticationRequest();
        } else {
            request = authentication;
        }

        // do not do anything for unsupported classes
        for (Class<?> clazz : notSupportedSimpleAuthenticationClasses) {
            if (clazz == request.getClass()) {
                return null;
            }
        }

        Authentication result = delegateProvider.authenticate(request);

        // if delegate returned null, returning null too as this means that
        // we shouldn't handle this
        if (result == null) {
            return null;
        }

        if (compoundAuthentication == null) {
            compoundAuthentication = new CompoundAuthentication();
        }
        compoundAuthentication.addReadyAuthentication(result);

        Authentication returnValue;
        if (authenticationPostProcessor != null) {
            returnValue = authenticationPostProcessor.postProcess(compoundAuthentication);
        } else {
            returnValue = compoundAuthentication;
        }
        return returnValue;
    }

    @Override
    protected boolean doSupports(Class<?> authentication) {
        if (CompoundAuthentication.class.isAssignableFrom(authentication)) {
            return true;
        }
        for (Class<?> clazz : supportedSimpleAuthenticationClasses) {
            if (clazz.equals(authentication)) {
                return true;
            }
        }
        return false;
    }

}
