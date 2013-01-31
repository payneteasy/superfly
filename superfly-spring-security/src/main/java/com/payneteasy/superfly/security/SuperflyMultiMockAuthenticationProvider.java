package com.payneteasy.superfly.security;

import java.util.Collections;
import java.util.Map;

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
	
	private String username;
	private String password;
	private String hotp;
	private ActionsMapBuilder actionsMapBuilder;
	private boolean enabled = true;
	
	private Map<SSORole, SSOAction[]> cachedMap = null;
	
	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
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
				if (username.equals(auth.getName()) && password.equals(auth.getCredentials())) {
					CompoundAuthentication newCompound = new CompoundAuthentication();
					newCompound.addReadyAuthentication(new UsernamePasswordCheckedToken(createSSOUser(auth.getName())));
					return newCompound;
				} else {
					throw new BadCredentialsException("Bad username/password");
				}
			} else if (auth instanceof CheckHOTPToken) {
                if (compound == null) {
                    throw new IllegalStateException("CompoundAuthentication cannot be null here");
                }
				CheckHOTPToken token = (CheckHOTPToken) auth;
				if (hotp.equals(token.getCredentials())) {
					if (token.getSsoUser().getActionsMap().size() == 1) {
						return new SSOUserAuthenticationToken(token.getSsoUser(), token.getSsoUser().getActionsMap().keySet().iterator().next(),
								token.getCredentials(), token.getDetails(), roleNameTransformers, roleSource);
					}
					CompoundAuthentication newCompound = new CompoundAuthentication(compound.getReadyAuthentications(), auth);
					newCompound.addReadyAuthentication(new HOTPCheckedToken(token.getSsoUser()));
					return newCompound;
				} else {
					throw new BadOTPValueException("Bad HOTP");
				}
			} else if (auth instanceof SSOUserAndSelectedRoleAuthenticationToken) {
				SSOUserAndSelectedRoleAuthenticationToken token = (SSOUserAndSelectedRoleAuthenticationToken) auth;
				return new SSOUserAuthenticationToken(token.getSsoUser(), token.getSsoRole(),
						token.getCredentials(), token.getDetails(), roleNameTransformers, roleSource);
			}
		}
		return null;
	}

	protected SSOUser createSSOUser(String username) {
		return new SSOUser(username, getActionsMap(), Collections.<String, String>emptyMap());
	}
	
	private Map<SSORole, SSOAction[]> getActionsMap() {
		if (cachedMap == null) {
			try {
				cachedMap = actionsMapBuilder.build();
			} catch (Exception e) {
				throw new AuthenticationServiceException("Counld not obtain roles and actions", e);
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
