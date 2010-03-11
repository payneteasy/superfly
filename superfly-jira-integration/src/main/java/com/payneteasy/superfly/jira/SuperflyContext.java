package com.payneteasy.superfly.jira;

import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.jira.init.UserStoreUpdater;

/**
 * Context of Superfly-related objects.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyContext {
	private SSOService ssoService;
	private String subsystemIdentifier;
	private UserStoreUpdater userStoreUpdater;

	public SSOService getSsoService() {
		return ssoService;
	}

	public void setSsoService(SSOService ssoService) {
		this.ssoService = ssoService;
	}

	public String getSubsystemIdentifier() {
		return subsystemIdentifier;
	}

	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	public UserStoreUpdater getUserStoreUpdater() {
		return userStoreUpdater;
	}

	public void setUserStoreUpdater(UserStoreUpdater userStoreUpdater) {
		this.userStoreUpdater = userStoreUpdater;
	}
}
