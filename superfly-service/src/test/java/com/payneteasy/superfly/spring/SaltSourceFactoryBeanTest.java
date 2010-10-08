package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.SaltSource;

import java.util.HashMap;
import java.util.Map;

public class SaltSourceFactoryBeanTest extends TestCase {
	public void testPlaintext() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("none");

        Map<String,SaltSource> ss=new HashMap<String, SaltSource>();

        ss.put("none",new NullSaltSource());

        factoryBean.setSalts(ss);
        
		SaltSource encoder = (SaltSource) factoryBean.getObject();
		assertEquals(encoder.getClass(), NullSaltSource.class);
	}
	
	public void testPciDss() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("pcidss");

        Map<String,SaltSource> ss=new HashMap<String, SaltSource>();

        //TODO  - change to RandomStoredSaltSource 
        ss.put("pcidss",new ConstantSaltSource());
        factoryBean.setSalts(ss);
        
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
