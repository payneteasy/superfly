package com.payneteasy.superfly.security.mapbuilder;

import com.payneteasy.superfly.api.SSOAction;

/**
 * Obtains somehow an array of actions and returns them.
 * 
 * @author Roman Puchkovskiy
 */
public interface ActionsSource {
	SSOAction[] getActions() throws Exception;
}
