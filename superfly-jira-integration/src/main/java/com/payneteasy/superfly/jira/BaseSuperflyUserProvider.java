package com.payneteasy.superfly.jira;

import java.util.List;
import java.util.Properties;

import com.opensymphony.user.Entity.Accessor;
import com.opensymphony.user.provider.UserProvider;
import com.payneteasy.superfly.api.SSOUserWithActions;
import com.payneteasy.superfly.common.store.GroupStoreLocator;
import com.payneteasy.superfly.common.store.StringStore;
import com.payneteasy.superfly.common.store.UserStore;
import com.payneteasy.superfly.common.store.UserStoreLocator;

/**
 * Base for Superfly-related implementations of interfaces extending
 * {@link UserProvider}.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class BaseSuperflyUserProvider implements UserProvider {

	public boolean create(String name) {
		// modifications are not supported
		return false;
	}

	public void flushCaches() {
	}

	public boolean init(Properties props) {
		return true;
	}

	public List<?> list() {
		return null;
	}

	public boolean load(String name, Accessor accessor) {
		accessor.setMutable(false);
		return true;
	}

	public boolean remove(String name) {
		// modifications are not supported
		return false;
	}

	public boolean store(String name, Accessor accessor) {
		// modifications are not supported
		return false;
	}
	
	protected UserStore getUserStore() {
		return UserStoreLocator.getUserStore();
	}
	
	protected StringStore getGroupStore() {
		return GroupStoreLocator.getGroupStore();
	}
	
	protected SSOUserWithActions getUserByName(String username) {
		return getUserStore().getUser(username);
	}
}
