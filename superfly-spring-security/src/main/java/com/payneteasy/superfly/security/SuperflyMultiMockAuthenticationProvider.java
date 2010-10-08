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
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
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
			if (authentication instanceof UsernamePasswordAuthRequestInfoAuthenticationToken) {
				if (username.equals(authentication.getName()) && password.equals(authentication.getCredentials())) {
					return new UsernamePasswordCheckedToken(createSSOUser(authentication.getName()));
				} else {
					throw new BadCredentialsException("Bad username/password");
				}
			} else if (authentication instanceof CheckHOTPToken) {
				CheckHOTPToken token = (CheckHOTPToken) authentication;
				if (hotp.equals(token.getCredentials())) {
					return new HOTPCheckedToken(token.getSsoUser());
				} else {
					throw new BadCredentialsException("Bad HOTP");
				}
			} else if (authentication instanceof SSOUserAndSelectedRoleAuthenticationToken) {
				SSOUserAndSelectedRoleAuthenticationToken token = (SSOUserAndSelectedRoleAuthenticationToken) authentication;
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

	public boolean supports(Class<? extends Object> authentication) {
		if (!enabled) {
			return false;
		}
		return (UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication)
				|| CheckHOTPToken.class.isAssignableFrom(authentication)
				|| SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication));
	}

}
