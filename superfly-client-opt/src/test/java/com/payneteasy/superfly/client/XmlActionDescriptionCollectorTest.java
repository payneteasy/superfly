package com.payneteasy.superfly.client;

import java.util.List;

import org.springframework.core.io.ClassPathResource;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.XmlActionDescriptionCollector;
import com.payneteasy.superfly.client.exception.CollectionException;

import junit.framework.TestCase;

public class XmlActionDescriptionCollectorTest extends TestCase {
	public void testCollect() throws CollectionException {
		XmlActionDescriptionCollector collector = new XmlActionDescriptionCollector();
		collector.setResource(new ClassPathResource("test-actions.xml"));
		List<ActionDescription> descriptions = collector.collect();
		
		assertNotNull("Null result", descriptions);
		assertEquals("Wrong number of actions collected", 2, descriptions.size());
		
		ActionDescription descr1 = descriptions.get(0);
		assertEquals("action1", descr1.getName());
		assertEquals("description1", descr1.getDescription());
		
		ActionDescription descr2 = descriptions.get(1);
		assertEquals("action2", descr2.getName());
		assertEquals("description2", descr2.getDescription());
	}
}
