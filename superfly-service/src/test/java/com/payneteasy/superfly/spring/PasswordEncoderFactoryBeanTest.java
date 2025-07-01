package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PasswordEncoder;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PasswordEncoderFactoryBeanTest {
    @Test
    public void testPlaintext() throws Exception {
//        PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
//        factoryBean.setPolicyName("none");
//        PasswordEncoder encoder = factoryBean.getObject();
//        assertEquals(encoder.getClass(), MessageDigestPasswordEncoder.class);
    }

    @Test
    public void testPciDss() throws Exception {
//        PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
//        factoryBean.setPolicyName("pcidss");
//        PasswordEncoder encoder = factoryBean.getObject();
//        assertEquals(encoder.getClass(), MessageDigestPasswordEncoder.class);
    }

    @Test
    public void testNull() throws Exception {
//        PasswordEncoderFactoryBean factoryBean = new PasswordEncoderFactoryBean();
//        factoryBean.setPolicyName(null);
//        try {
//            factoryBean.getObject();
//            Assert.fail();
//        } catch (IllegalArgumentException e) {
//            // expected
//        }
    }
}
