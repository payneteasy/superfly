package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.service.impl.TrivialProxyFactory;
import com.payneteasy.superfly.spi.HOTPProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.ListableBeanFactory;

public class HOTPProviderFactoryBeanTest {

    private HOTPProviderFactoryBean factoryBean;

    @Before
    public void setUp() throws Exception {
        factoryBean = new HOTPProviderFactoryBean();
        factoryBean.setBeanFactory(TrivialProxyFactory.createProxy(ListableBeanFactory.class));
        factoryBean.setAllowTestProvider(true);
        factoryBean.afterPropertiesSet();
    }

    @Test
    public void testInstantiation() throws Exception {
        Assert.assertEquals(HOTPProvider.class, factoryBean.getObjectType());
        Object object = factoryBean.getObject();
        Assert.assertTrue("Got: " + object, object instanceof TestHOTPProvider);
        Assert.assertTrue(((TestHOTPProvider) object).isInitialized());
    }
}
