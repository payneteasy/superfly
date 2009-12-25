package com.payneteasy.superfly.api;

import java.io.Serializable;
import java.util.Map;

/**
 * User from SSO with roles, actions and preferences. User may have several
 * roles, each role has its own action set.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOUser implements Serializable {
	private String name;
	private Map<SSORole, SSOAction[]> actionsMap;
	private Map<String, String> preferences;

	/**
	 * Constructs user.
	 * 
	 * @param name			user name
	 * @param actionsMap	mapping from roles to action sets
	 * @param preferences	preferences of the user
	 */
	public SSOUser(String name, Map<SSORole, SSOAction[]> actionsMap,
			Map<String, String> preferences) {
		super();
		this.name = name;
		this.actionsMap = actionsMap;
		this.preferences = preferences;
	}

	/**
	 * Returns user name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets user name.
	 * 
	 * @param name	name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a mapping from roles to action sets.
	 * 
	 * @return mapping
	 */
	public Map<SSORole, SSOAction[]> getActionsMap() {
		return actionsMap;
	}

	/**
	 * Sets a mapping from roles to action sets.
	 * 
	 * @param actionsMap
	 */
	public void setActionsMap(Map<SSORole, SSOAction[]> actionsMap) {
		this.actionsMap = actionsMap;
	}

	/**
	 * Returns preferences.
	 * 
	 * @return preferences
	 */
	public Map<String, String> getPreferences() {
		return preferences;
	}

	/**
	 * Sets preferences.
	 * 
	 * @param preferences	preferences to set
	 */
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	@Override
	public String toString() {
		return name;
	}
}
