package com.payneteasy.superfly.security_2_0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.client.ActionDescriptionCollector;
import com.payneteasy.superfly.client.exception.CollectionException;
import com.payneteasy.superfly.security_2_0.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security_2_0.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security_2_0.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;

public class SuperflyMockAuthenticationProvider extends SuperflyAuthenticationProvider {
	
	private String username;
	private String password;
	private List<String> roleNames = new ArrayList<String>();
	private ActionDescriptionCollector actionDescriptionCollector;
	private Map<SSORole, SSOAction[]> cachedActionsMap = null;
	
	@Required
	public void setUsername(String username) {
		this.username = username;
	}

	@Required
	public void setPassword(String password) {
		this.password = password;
	}

	public void setRoleNames(List<String> roleNames) {
		this.roleNames = roleNames;
	}

	@Required
	public void setActionDescriptionCollector(
			ActionDescriptionCollector actionDescriptionCollector) {
		this.actionDescriptionCollector = actionDescriptionCollector;
	}
	
	@Override
	protected SSOUser doAuthenticate(
			UsernamePasswordAuthRequestInfoAuthenticationToken authRequest,
			String username, String password) {
		boolean ok = this.username.equals(username) && this.password.equals(password);
		
		if (!ok) {
			throw new BadCredentialsException("Bad password");
		}
		
		if (roleNames == null || roleNames.isEmpty()) {
			throw new BadCredentialsException("No roles assigned");
		}
		
		SSOUser ssoUser;
		try {
			ssoUser = new SSOUser(username, buildActionsMap(username), buildPreferences());
		} catch (CollectionException e) {
			throw new AuthenticationServiceException("Cannot collect action descriptions", e);
		}
		return ssoUser;
	}

	protected Map<SSORole, SSOAction[]> buildActionsMap(String username) throws CollectionException {
		if (cachedActionsMap == null) {
			List<ActionDescription> actionDescriptions = actionDescriptionCollector.collect();
			cachedActionsMap = new HashMap<SSORole, SSOAction[]>();
			for (String roleName : roleNames) {
				SSORole role = new SSORole(roleName);
				SSOAction[] actions = new SSOAction[actionDescriptions.size()];
				for (int i = 0; i < actionDescriptions.size(); i++) {
					actions[i] = new SSOAction(actionDescriptions.get(i).getName(), false);
				}
				cachedActionsMap.put(role, actions);
			}
		}
		return cachedActionsMap;
	}
	
	protected Map<String, String> buildPreferences() {
		return Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	public boolean supports(Class authentication) {
		return UsernamePasswordAuthRequestInfoAuthenticationToken.class.isAssignableFrom(authentication)
		|| SSOUserTransportAuthenticationToken.class.isAssignableFrom(authentication)
		|| SSOUserAndSelectedRoleAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
