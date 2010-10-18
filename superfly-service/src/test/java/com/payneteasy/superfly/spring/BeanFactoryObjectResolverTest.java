package com.payneteasy.superfly.spring;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.payneteasy.superfly.spring.classes.Iface;

public class BeanFactoryObjectResolverTest extends TestCase {
	private ListableBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource("test-ObjectResolver.xml"));
	private BeanFactoryObjectResolver resolver;
	
	public void setUp() {
		resolver = new BeanFactoryObjectResolver(beanFactory);
	}
	
	public void testFindObject() {
		Object object = resolver.resolve(Iface.class);
		assertNotNull(object);
		assertTrue(object instanceof Iface);
	}
	
	public void testNullObject() {
		Object object = resolver.resolve(BeanFactory.class);
		assertNull(object);
	}
}
