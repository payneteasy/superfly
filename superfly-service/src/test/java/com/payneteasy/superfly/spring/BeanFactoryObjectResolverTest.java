package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.spring.classes.Iface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class BeanFactoryObjectResolverTest {
    private ListableBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("test-ObjectResolver.xml"));
    private BeanFactoryObjectResolver resolver;

    @Before
    public void setUp() {
        resolver = new BeanFactoryObjectResolver(beanFactory);
    }

    @Test
    public void testFindObject() {
        Object object = resolver.resolve(Iface.class);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof Iface);
    }

    @Test
    public void testNullObject() {
        Object object = resolver.resolve(BeanFactory.class);
        Assert.assertNull(object);
    }
}
