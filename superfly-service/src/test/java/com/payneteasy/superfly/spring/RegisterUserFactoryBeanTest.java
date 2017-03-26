package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.register.pcidss.PCIDSSRegisterUserStrategy;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegisterUserFactoryBeanTest {
    @Test
    public void testNone() throws Exception {
        RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
        bean.setPolicyName("none");
        RegisterUserStrategy registerUserStrategy = bean.getObject();
        assertEquals(registerUserStrategy.getClass(), NoneRegisterUserStrategy.class);
    }

    @Test
    public void testPCIDSS() throws Exception {
        RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
        bean.setPolicyName("pcidss");
        RegisterUserStrategy registerUserStrategy = bean.getObject();
        assertEquals(registerUserStrategy.getClass(), PCIDSSRegisterUserStrategy.class);
    }

    @Test
    public void testNull() throws Exception {
        RegisterUserStrategyFactoryBean bean = new RegisterUserStrategyFactoryBean();
        bean.setPolicyName(null);
        try {
            bean.getObject();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
