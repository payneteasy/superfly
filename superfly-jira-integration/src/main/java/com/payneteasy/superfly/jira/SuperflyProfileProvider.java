package com.payneteasy.superfly.jira;

import java.util.HashMap;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.user.provider.ProfileProvider;

/**
 * Superfly-related {@link ProfileProvider} implementation
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyProfileProvider extends BaseSuperflyUserProvider implements
		ProfileProvider {
	private static final String PROPERTY_SET_NAME = "superfly";
	
	public static final String USER_NAME = "name";

	public PropertySet getPropertySet(String name) {
		Map<String, String> args = new HashMap<String, String>();
		
		args.put(USER_NAME, name);
		
		return PropertySetManager.getInstance(PROPERTY_SET_NAME, args);
	}
	
	public boolean handles(String name) {
		return getUserStore().userExists(name);
	}

}
