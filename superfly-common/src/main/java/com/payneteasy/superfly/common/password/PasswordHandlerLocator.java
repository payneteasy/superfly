package com.payneteasy.superfly.common.password;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Simple {@link PasswordHandler} locator which, by default, just locates
 * a {@link ShaPasswordHandler}.
 * 
 * @author Roman Puchkovskiy
 */
public class PasswordHandlerLocator {
	private static SingletonHolder<PasswordHandler> passwordHandlerHolder = new SingletonHolder<PasswordHandler>() {
		@Override
		protected PasswordHandler createInstance() {
			return createPasswordHandler();
		}
	};
	
	public static PasswordHandler getPasswordHandler() {
		return passwordHandlerHolder.getInstance();
	}

	protected static PasswordHandler createPasswordHandler() {
		return new ShaPasswordHandler();
	}
}
