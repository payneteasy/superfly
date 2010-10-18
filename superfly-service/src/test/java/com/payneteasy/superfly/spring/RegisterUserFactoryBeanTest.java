package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.register.pcidss.PCIDSSRegisterUserStrategy;

public class RegisterUserFactoryBeanTest extends TestCase{
	public void testNone() throws Exception {
		RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
		bean.setPolicyName("none");
		RegisterUserStrategy registerUserStrategy = (RegisterUserStrategy)bean.getObject();
		assertEquals(registerUserStrategy.getClass(), NoneRegisterUserStrategy.class);
	}
	
	public void testPCIDSS() throws Exception {
		RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
		bean.setPolicyName("pcidss");
		RegisterUserStrategy registerUserStrategy = (RegisterUserStrategy)bean.getObject();
		assertEquals(registerUserStrategy.getClass(), PCIDSSRegisterUserStrategy.class);
	}
	
	public void testNull() throws Exception {
		RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
		bean.setPolicyName(null);
		try {
			bean.getObject();
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
