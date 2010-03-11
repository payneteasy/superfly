package com.payneteasy.superfly.jira;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * Provides Superfly-related property set.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyPropertySet extends AbstractPropertySet {
	
	private String username;
	private Map<String, String> props;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(Map config, Map args) {
		super.init(config, args);
		username = (String) args.get(SuperflyProfileProvider.USER_NAME);
		props = new HashMap<String, String>();
	}

	@Override
	public boolean exists(String key) throws PropertyException {
		return props.containsKey(key);
	}

	@Override
	protected Object get(int type, String key) throws PropertyException {
		return props.get(key);
	}

	@Override
	public Collection<String> getKeys(String prefix, int type) throws PropertyException {
		Set<String> result = new HashSet<String>();
		for (String key : props.keySet()) {
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
