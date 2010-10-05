package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.SaltSource;

public class SaltSourceFactoryBeanTest extends TestCase {
	public void testPlaintext() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("none");
		SaltSource encoder = (SaltSource) factoryBean.getObject();
		assertEquals(encoder.getClass(), NullSaltSource.class);
	}
	
	public void testPciDss() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("pcidss");
		SaltSource encoder = (SaltSource) factoryBean.getObject();
		assertEquals(encoder.getClass(), ConstantSaltSource.class);
	}
	
	public void testNull() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName(null);
		try {
			factoryBean.getObject();
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
