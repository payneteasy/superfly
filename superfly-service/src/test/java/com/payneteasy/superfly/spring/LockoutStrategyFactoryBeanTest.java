package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LockoutStrategyFactoryBeanTest {
    @Test
    public void testNone() throws Exception {
        LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
        bean.setPolicyName("none");
        LockoutStrategy lockoutStrategy = bean.getObject();
        assertEquals(lockoutStrategy.getClass(), NoneLockoutStrategy.class);
    }

    @Test
    public void testPCIDSS() throws Exception {
        LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
        bean.setPolicyName("pcidss");
        LockoutStrategy lockoutStrategy = bean.getObject();
        assertEquals(lockoutStrategy.getClass(), PCIDSSLockoutStrategy.class);
    }

    @Test
    public void testNull() throws Exception {
        LockoutStrategyFactoryBean bean = new LockoutStrategyFactoryBean();
        bean.setPolicyName(null);
        try {
            bean.getObject();
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
