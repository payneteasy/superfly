package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;

import junit.framework.TestCase;

public class LockoutStrategyFactoryBeanTest extends TestCase {
	public void testNone() throws Exception {
		LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
		bean.setPolicyName("none");
		LockoutStrategy lockoutStrategy = (LockoutStrategy)bean.getObject();
		assertEquals(lockoutStrategy.getClass(), NoneLockoutStrategy.class);
	}
	
	public void testPCIDSS() throws Exception {
		LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
		bean.setPolicyName("pcidss");
		LockoutStrategy lockoutStrategy = (LockoutStrategy)bean.getObject();
		assertEquals(lockoutStrategy.getClass(), PCIDSSLockoutStrategy.class);
	}
	
	public void testNull() throws Exception {
		LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
		bean.setPolicyName(null);
		try {
			bean.getObject();
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
