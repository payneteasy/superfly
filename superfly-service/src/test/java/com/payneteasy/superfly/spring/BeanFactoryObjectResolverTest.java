package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.spring.classes.Iface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

public class BeanFactoryObjectResolverTest {
    private BeanFactoryObjectResolver resolver;

    @Before
    public void setUp() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("test-ObjectResolver.xml"));

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
