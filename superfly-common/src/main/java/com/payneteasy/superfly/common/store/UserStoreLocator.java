package com.payneteasy.superfly.common.store;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Simple {@link UserStore} locator.
 * 
 * @author Roman Puchkovskiy
 */
public class UserStoreLocator {
	private static SingletonHolder<UserStore> userStoreHolder = new SingletonHolder<UserStore>() {
		@Override
		protected UserStore createInstance() {
			return createUserStore();
		}
	};
	
	public static UserStore getUserStore() {
		return userStoreHolder.getInstance();
	}

	protected static UserStore createUserStore() {
		return new SimpleUserStore();
	}
}
