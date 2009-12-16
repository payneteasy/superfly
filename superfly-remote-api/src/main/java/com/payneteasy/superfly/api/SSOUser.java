package com.payneteasy.superfly.api;

import java.io.Serializable;
import java.util.Map;

/**
 * User from SSO with roles, actions and preferences.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOUser implements Serializable {
	private String name;
	private Map<SSORole, SSOAction[]> actionsMap;
	private Map<String, String> preferences;

	public SSOUser(String name, Map<SSORole, SSOAction[]> actionsMap,
			Map<String, String> preferences) {
		super();
		this.name = name;
		this.actionsMap = actionsMap;
		this.preferences = preferences;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<SSORole, SSOAction[]> getActionsMap() {
		return actionsMap;
	}

	public void setActionsMap(Map<SSORole, SSOAction[]> actionsMap) {
		this.actionsMap = actionsMap;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
}
