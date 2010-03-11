package com.payneteasy.superfly.jira.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;

/**
 * Provides Superfly-related property set.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyPropertySet extends AbstractPropertySet {
	
	private String username;
	private String email;
	private Map<String, String> attrsMapping;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(Map config, Map args) {
		super.init(config, args);
		username = (String) args.get(SuperflyProfileProvider.USER_NAME);
		email = (String) args.get(SuperflyProfileProvider.EMAIL);
		attrsMapping = new HashMap<String, String>();
	}

	@Override
	public boolean exists(String key) throws PropertyException {
		if (User.PROPERTY_FULLNAME.equals(key)) {
			return true;
		}
		if (User.PROPERTY_EMAIL.equals(key)) {
			return true;
		}
		return attrsMapping.containsKey(key);
	}

	@Override
	protected Object get(int type, String key) throws PropertyException {
		if (User.PROPERTY_FULLNAME.equals(key)) {
			return username;
		}
		if (User.PROPERTY_EMAIL.equals(key)) {
			return email;
		}
		return attrsMapping.get(key);
	}

	@Override
	public Collection<String> getKeys(String prefix, int type) throws PropertyException {
		if (prefix == null) {
			prefix = "";
		}
		Set<String> result = new HashSet<String>();
		if (User.PROPERTY_FULLNAME.startsWith(prefix)) {
			result.add(User.PROPERTY_FULLNAME);
		}
		if (User.PROPERTY_EMAIL.startsWith(prefix)) {
			result.add(User.PROPERTY_EMAIL);
		}
		for (String key : attrsMapping.keySet()) {
			if (key.startsWith(prefix)) {
				result.add(key);
			}
		}
		return result;
	}

	@Override
	public boolean supportsType(int type) {
		return type == PropertySet.STRING;
	}

	@Override
	public int getType(String key) throws PropertyException {
		if (User.PROPERTY_FULLNAME.equals(key)) {
			return PropertySet.STRING;
		}
		if (User.PROPERTY_EMAIL.equals(key)) {
			return PropertySet.STRING;
		}
		return exists(key) ? PropertySet.STRING : 0;
	}

	@Override
	public void remove(String key) throws PropertyException {
		// modifications are not supported
	}

	@Override
	protected void setImpl(int type, String key, Object value)
			throws PropertyException {
		// modifications are not supported
	}

}
