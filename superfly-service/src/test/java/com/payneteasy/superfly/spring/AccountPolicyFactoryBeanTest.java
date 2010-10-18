package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.policy.account.none.SimpleAccountPolicy;

import junit.framework.TestCase;

public class AccountPolicyFactoryBeanTest extends TestCase {
	private AccountPolicyFactoryBean factoryBean;
	
	public void setUp() {
		factoryBean = new AccountPolicyFactoryBean();
	}
	
	public void testNone() throws Exception {
		factoryBean.setPolicyName("none");
		assertTrue(factoryBean.getObject() instanceof SimpleAccountPolicy);
	}
}
