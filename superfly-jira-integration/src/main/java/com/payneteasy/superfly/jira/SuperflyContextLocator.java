package com.payneteasy.superfly.jira;

import com.payneteasy.superfly.common.singleton.SingletonHolder;

/**
 * Locator for a Superfly context.
 * 
 * @author Roman Puchkovskiy
 */
public class SuperflyContextLocator {
	private static final SingletonHolder<SuperflyContext> superflyContextHolder = new SingletonHolder<SuperflyContext>() {
		@Override
		protected SuperflyContext createInstance() {
			return createSuperflyContext();
		}
	};

	protected static SuperflyContext createSuperflyContext() {
		return new SuperflyContext();
	}
	
	public static SuperflyContext getSuperflyContext() {
		return superflyContextHolder.getInstance();
	}
}
