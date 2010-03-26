package com.payneteasy.superfly.security;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

public class CompoundRoleSource implements RoleSource {
	
	private RoleSource[] roleSources;
	
	public CompoundRoleSource(RoleSource[] roleSources) {
		this.roleSources = roleSources;
	}

	public String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole) {
		List<String> resultList = new ArrayList<String>();
		for (RoleSource roleSource : roleSources) {
			String[] actionNames = roleSource.getRoleNames(ssoUser, ssoRole);
			for (String actionName : actionNames) {
				resultList.add(actionName);
			}
		}
		return resultList.toArray(new String[resultList.size()]);
	}

}
