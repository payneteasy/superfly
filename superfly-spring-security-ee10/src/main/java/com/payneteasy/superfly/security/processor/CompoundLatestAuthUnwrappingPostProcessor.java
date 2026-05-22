package com.payneteasy.superfly.security.processor;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import org.springframework.security.core.Authentication;

/**
 * Post-processor which replaces a {@link CompoundAuthentication} with its
 * latest ready {@link Authentication}.
 *
 * @author Roman Puchkovskiy
 */
public class CompoundLatestAuthUnwrappingPostProcessor implements AuthenticationPostProcessor {

    public Authentication postProcess(Authentication auth) {
        CompoundAuthentication compound = (CompoundAuthentication) auth;
        return compound.getLatestReadyAuthentication();
    }

}
