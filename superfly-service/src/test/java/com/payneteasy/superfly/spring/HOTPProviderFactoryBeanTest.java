package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import org.springframework.beans.factory.ListableBeanFactory;

import com.payneteasy.superfly.service.impl.TrivialProxyFactory;
import com.payneteasy.superfly.spi.HOTPProvider;

public class HOTPProviderFactoryBeanTest extends TestCase {
	
	private HOTPProviderFactoryBean factoryBean;
	
	public void setUp() throws Exception {
		factoryBean = new HOTPProviderFactoryBean();
		factoryBean.setBeanFactory(TrivialProxyFactory.createProxy(ListableBeanFactory.class));
		factoryBean.setAllowTestProvider(true);
		factoryBean.afterPropertiesSet();
	}
	
	public void testInstantiation() throws Exception {
		assertEquals(HOTPProvider.class, factoryBean.getObjectType());
		Object object = factoryBean.getObject();
		assertTrue("Got: " + object, object instanceof TestHOTPProvider);
		assertTrue(((TestHOTPProvider) object).isInitialized());
	}
}
