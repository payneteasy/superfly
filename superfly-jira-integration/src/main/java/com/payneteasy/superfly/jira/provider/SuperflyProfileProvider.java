package com.payneteasy.superfly.jira.provider;

import java.util.HashMap;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.user.provider.ProfileProvider;
import com.payneteasy.superfly.api.SSOUserWithActions;

/**
 * Superfly-related {@link ProfileProvider} implementation.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyProfileProvider extends BaseSuperflyUserProvider implements
		ProfileProvider {
	private static final String PROPERTY_SET_NAME = "superfly";
	
	public static final String USER_NAME = "name";
	public static final String EMAIL = "email";

	public PropertySet getPropertySet(String name) {
		Map<String, String> args = new HashMap<String, String>();
		
		SSOUserWithActions user = getUserByName(name);
		String email = null;
		if (user != null) {
			email = user.getEmail();
		}
		
		args.put(USER_NAME, name);
		args.put(EMAIL, email);
		
		return PropertySetManager.getInstance(PROPERTY_SET_NAME, args);
	}
	
	public boolean handles(String name) {
		return getUserStore().userExists(name);
	}

}
