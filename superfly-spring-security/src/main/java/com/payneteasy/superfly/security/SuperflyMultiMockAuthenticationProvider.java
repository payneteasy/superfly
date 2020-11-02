package com.payneteasy.superfly.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.payneteasy.superfly.security.processor.AuthenticationPostProcessor;
import com.payneteasy.superfly.security.processor.IdAuthenticationPostProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;

/**
 * {@link AuthenticationProvider} which may be used for rapid development using
 * local mode (with no interaction with Superfly server).
 *
 * @author Roman Puchkovskiy
 */
public class SuperflyMultiMockAuthenticationProvider extends
        AbstractRoleTransformingAuthenticationProvider {

    private final Map<String, String> usernamesToPasswords = new HashMap<>();
    private String hotp;
    private ActionsMapBuilder actionsMapBuilder;
    private boolean enabled = true;
    private AuthenticationPostProcessor authenticationPostProcessor = new IdAuthenticationPostProcessor();

    private Map<SSORole, SSOAction[]> cachedMap = null;

    public void addUsernameAndPassword(String username, String password) {
        usernamesToPasswords.put(username, password);
    }

    public void setHotp(String hotp) {
        this.hotp = hotp;
    }

    @Required
    public void setActionsMapBuilder(ActionsMapBuilder actionsMapBuilder) {
        this.actionsMapBuilder = actionsMapBuilder;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAuthenticationPostProcessor(
            AuthenticationPostProcessor authenticationPostProcessor) {
        this.authenticationPostProcessor = authenticationPostProcessor;
    }

    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (enabled) {
            Authentication auth;
            CompoundAuthentication compound = null;
            if (authentication instanceof CompoundAuthentication) {
                compound = (CompoundAuthentication) authentication;
                auth = compound.getCurrentAuthenticationRequest();
            } else {
                auth = authentication;
            }
            if (auth instanceof UsernamePasswordAuthRequestInfoAuthenticationToken) {
                return processUsernamePasswordAuth(auth);
            } else if (auth instanceof CheckHOTPToken) {
                CheckHOTPToken token = (CheckHOTPToken) auth;
                if (hotp.equals(token.getCredentials())) {
                    if (token.getSsoUser().getActionsMap().size() == 1) {
                        return new SSOUserAuthenticationToken(token.getSsoUser(), token.getSsoUser().getActionsMap().keySet().iterator().next(),
                                token.getCredentials(), token.getDetails(), roleNameTransformers, roleSource);
                    }
                    if (compound == null) {
                        throw new IllegalStateException("CompoundAuthentication cannot be null here");
                    }
                    CompoundAuthentication newCompound = new CompoundAuthentication(compound.getReadyAuthentications(), auth);
                    newCompound.addReadyAuthentication(new HOTPCheckedToken(token.getSsoUser()));
                    return newCompound;
                } else {
                    throw new BadOTPValueException("Bad HOTP");
                }
            } else if (auth instanceof SSOUserAndSelectedRoleAuthenticationToken) {
                SSOUserAndSelectedRoleAuthenticationToken token = (SSOUserAndSelectedRoleAuthenticationToken) auth;
                final SSOUserAuthenticationToken ssoUserAuthenticationToken = new SSOUserAuthenticationToken(
                        token.getSsoUser(), token.getSsoRole(),
                        token.getCredentials(), token.getDetails(), roleNameTransformers, roleSource);
                return authenticationPostProcessor.postProcess(ssoUserAuthenticationToken);
            }
        }
        return null;
    }

    protected Authentication processUsernamePasswordAuth(Authentication auth) {
        String username = auth.getName();
        String password = auth.getCredentials() == null ? null : auth.getCredentials().toString();
        if (checkUsernamePassword(username, password)) {
            CompoundAuthentication newCompound = new CompoundAuthentication();
            newCompound.addReadyAuthentication(new UsernamePasswordCheckedToken(createSSOUser(username)));
            return newCompound;
        } else {
            throw new BadCredentialsException("Bad username/password");
        }
    }

    protected boolean checkUsernamePassword(String username, String password) {
        return Objects.equals(usernamesToPasswords.get(username), password);
    }

    protected SSOUser createSSOUser(String username) {
        return new SSOUser(username, getActionsMap(username), Collections.<String, String>emptyMap());
    }

    protected Map<SSORole, SSOAction[]> getActionsMap(String username) {
        if (cachedMap == null) {
            try {
                cachedMap = actionsMapBuilder.build();
            } catch (Exception e) {
                throw new AuthenticationServiceException("Could not obtain roles and actions", e);
            }
        }
        return cachedMap;
    }

    public boolean supports(Class<?> authentication) {
        if (!enabled) {
            return false;
        }
        return (UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication)
                || CheckHOTPToken.class.isAssignableFrom(authentication)
                || SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication)
                || CompoundAuthentication.class.isAssignableFrom(authentication));
    }

}
