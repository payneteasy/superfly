package com.payneteasy.superfly.jira.provider;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.user.provider.CredentialsProvider;
import com.payneteasy.superfly.api.SSOUserWithActions;

/**
 * Superfly-related {@link CredentialsProvider} implementation.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyCredentialsProvider extends BaseSuperflyUserProvider implements
		CredentialsProvider {
	
	public boolean handles(String username) {
		return getUserStore().userExists(username);
	}
	
	@Override
	public List<String> list() {
		List<String> userNames = new ArrayList<String>();
		for (SSOUserWithActions user : getUserStore().getUsers()) {
			userNames.add(user.getName());
		}
		return userNames;
	}

	public boolean authenticate(String username, String password) {
		// NOTE: not sure is this ok, but this seems to work, possibly because
		// user has already been authenticated by Authenticator. Superfly
		// authentication is heavy, so we cannot re-authenticate user on any
		// action...
		return false;
	}

	public boolean changePassword(String username, String password) {
		// modifications are not supported
		return true;
	}

}
