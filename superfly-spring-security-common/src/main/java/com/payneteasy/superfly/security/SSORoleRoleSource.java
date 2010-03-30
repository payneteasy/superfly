package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

public class SSORoleRoleSource implements RoleSource {

	public String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole) {
		String[] result = new String[1];
		result[0] = ssoRole.getName();
		return result;
	}

}
