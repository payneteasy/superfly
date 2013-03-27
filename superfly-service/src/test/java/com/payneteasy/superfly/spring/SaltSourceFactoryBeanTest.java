package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.password.ConstantSaltSource;
import com.payneteasy.superfly.password.NullSaltSource;
import com.payneteasy.superfly.password.SaltSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SaltSourceFactoryBeanTest {
    @Test
	public void testPlaintext() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("none");

        Map<String,SaltSource> ss=new HashMap<String, SaltSource>();

        ss.put("none",new NullSaltSource());

        factoryBean.setSalts(ss);
        
		SaltSource encoder = factoryBean.getObject();
        assertEquals(encoder.getClass(), NullSaltSource.class);
	}

    @Test
	public void testPciDss() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName("pcidss");

        Map<String,SaltSource> ss=new HashMap<String, SaltSource>();

        //TODO  - change to RandomStoredSaltSource 
        ss.put("pcidss",new ConstantSaltSource());
        factoryBean.setSalts(ss);
        
		SaltSource encoder = factoryBean.getObject();
		assertEquals(encoder.getClass(), ConstantSaltSource.class);
	}

    @Test
	public void testNull() throws Exception {
		SaltSourceFactoryBean factoryBean = new SaltSourceFactoryBean();
		factoryBean.setPolicyName(null);
		try {
			factoryBean.getObject();
            Assert.fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
}
