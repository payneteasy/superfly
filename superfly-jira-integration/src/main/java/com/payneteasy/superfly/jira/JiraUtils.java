package com.payneteasy.superfly.jira;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSOUserWithActions;

/**
 * Code used by the library.
 * 
 * @author Roman Puchkovskiy
 */
public class JiraUtils {
	private JiraUtils() {}

	/**
	 * Returns true if a user has requested action.
	 * 
	 * @param user			user
	 * @param actionName	name of the action to check
	 * @return true if user has this action
	 */
	public static boolean userHasAction(SSOUserWithActions user, String actionName) {
		boolean result = false;
		for (SSOAction action : user.getActions()) {
			if (actionName.equals(action.getName())) {
				result = true;
				break;
			}
		}
		return result;
	}
}
