package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

public class SSOActionRoleSource implements RoleSource {

	public String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole) {
		SSOAction[] actions = ssoUser.getActionsMap().get(ssoRole);
		String[] result = new String[actions.length];
		for (int i = 0; i < actions.length; i++) {
			result[i] = actions[i].getName();
		}
		return result;
	}

}
