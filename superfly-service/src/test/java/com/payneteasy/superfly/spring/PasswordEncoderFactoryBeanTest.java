package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;

public class PasswordEncoderFactoryBeanTest extends TestCase {
	public void testPlaintext() throws Exception {
		PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
		factoryBean.setPolicyName("none");
		PasswordEncoder encoder = (PasswordEncoder) factoryBean.getObject();
		assertEquals(encoder.getClass(), PlaintextPasswordEncoder.class);
	}
	
	public void testPciDss() throws Exception {
		PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
		factoryBean.setPolicyName("pcidss");
		PasswordEncoder encoder = (PasswordEncoder) factoryBean.getObject();
		assertEquals(encoder.getClass(), MessageDigestPasswordEncoder.class);
	}
	
	public void testNull() throws Exception {
		PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
		factoryBean.setPolicyName(null);
		try {
			factoryBean.getObject();
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
