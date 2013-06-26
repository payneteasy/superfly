package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.policy.account.none.SimpleAccountPolicy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountPolicyFactoryBeanTest {
	private AccountPolicyFactoryBean factoryBean;

    @Before
	public void setUp() {
		factoryBean = new AccountPolicyFactoryBean();
	}

    @Test
	public void testNone() throws Exception {
		factoryBean.setPolicyName("none");
        Assert.assertTrue(factoryBean.getObject() instanceof SimpleAccountPolicy);
	}
}
