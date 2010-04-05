package com.payneteasy.superfly.security.mapbuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;

/**
 * {@link ActionsMapBuilder} implementation in which it's possible to specify
 * actions for each role separately.
 * 
 * @author Roman Puchkovskiy
 */
public class SeparateActionsMapBuilder implements ActionsMapBuilder {

	private Map<String, ActionsSource> roleNamesToActionsSources;

	@Required
	public void setRoleNamesToActionsSources(
			Map<String, ActionsSource> roleNamesToActionsSources) {
		this.roleNamesToActionsSources = roleNamesToActionsSources;
	}

	public Map<SSORole, SSOAction[]> build() throws Exception {
		Map<SSORole, SSOAction[]> map = new HashMap<SSORole, SSOAction[]>();
		for (Entry<String, ActionsSource> entry : roleNamesToActionsSources.entrySet()) {
			map.put(new SSORole(entry.getKey()), entry.getValue().getActions());
		}
		return map;
	}

}
