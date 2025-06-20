package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Collections;
import java.util.Map;

/**
 * Authentication provider which does not use Superfly server at all. It may
 * be useful for fast development.
 *
 * @author Roman Puchkovskiy
 */
public class SuperflyMockAuthenticationProvider extends AbstractSuperflyAuthenticationProvider {

    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private ActionsMapBuilder actionsMapBuilder;

    @Setter
    private Map<SSORole, SSOAction[]> cachedActionsMap;

    public SuperflyMockAuthenticationProvider() {
    }

    public SuperflyMockAuthenticationProvider(String username, String password, ActionsMapBuilder actionsMapBuilder) {
        this.username = username;
        this.password = password;
        this.actionsMapBuilder = actionsMapBuilder;
    }

    @Override
    protected SSOUser doAuthenticate(
            UsernamePasswordAuthRequestInfoAuthenticationToken authRequest,
            String username,
            String password
    ) {
        boolean ok = this.username.equals(username) && this.password.equals(password);

        if (!ok) {
            throw new BadCredentialsException("Bad password");
        }

        try {
            return new SSOUser(username, buildActionsMap(), buildPreferences());
        } catch (Exception e) {
            throw new AuthenticationServiceException("Cannot collect action descriptions", e);
        }
    }

    protected Map<SSORole, SSOAction[]> buildActionsMap() throws Exception {
        if (cachedActionsMap == null) {
            cachedActionsMap = actionsMapBuilder.build();
        }
        return cachedActionsMap;
    }

    protected Map<String, String> buildPreferences() {
        return Collections.emptyMap();
    }

}
