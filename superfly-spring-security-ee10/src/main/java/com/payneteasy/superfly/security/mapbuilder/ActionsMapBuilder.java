package com.payneteasy.superfly.security.mapbuilder;

import java.util.Map;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;

/**
 * Interface for building a mapping from an SSORole to SSOAction's. Mainly
 * used by {@link SuperflyMockAuthenticationProvider}.
 * 
 * @author Roman Puchkovskiy
 */
public interface ActionsMapBuilder {
    Map<SSORole, SSOAction[]> build() throws Exception;
}
