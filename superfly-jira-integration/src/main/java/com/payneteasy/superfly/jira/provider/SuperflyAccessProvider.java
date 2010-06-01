package com.payneteasy.superfly.jira.provider;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.user.provider.AccessProvider;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.jira.JiraUtils;

/**
 * Superfly-related {@link AccessProvider} implementation.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyAccessProvider extends BaseSuperflyUserProvider implements
		AccessProvider {
	private static final long serialVersionUID = -68588532298857581L;

	public boolean handles(String name) {
		if (getUserStore().userExists(name)) {
			return true;
		}
		if (getGroupStore().exists(name)) {
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> list() {
		return new ArrayList<String>(getGroupStore().getObjects());
	}

	public boolean addToGroup(String username, String groupname) {
		// modifications are not supported
		return true;
	}

	public boolean inGroup(String username, String groupname) {
		SSOUserWithActions user = getUserByName(username);
		if (user == null) {
			return false;
		}
		return JiraUtils.userHasAction(user, groupname);
	}

	public List<String> listGroupsContainingUser(String username) {
		SSOUserWithActions user = getUserByName(username);
		List<String> groupNames = new ArrayList<String>(user.getActions().length);
		for (SSOAction action : user.getActions()) {
			groupNames.add(action.getName());
		}
		return groupNames;
	}

	public List<String> listUsersInGroup(String groupname) {
		List<String> userNames = new ArrayList<String>();
		for (SSOUserWithActions user : getUserStore().getUsers()) {
			if (JiraUtils.userHasAction(user, groupname)) {
				userNames.add(user.getName());
			}
		}
		return userNames;
	}

	public boolean removeFromGroup(String arg0, String arg1) {
		// modifications are not supported
		return true;
	}

}
