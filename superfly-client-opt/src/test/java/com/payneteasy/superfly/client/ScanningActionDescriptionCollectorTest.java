package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.junit.Test;
import org.springframework.security.access.annotation.Secured;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ScanningActionDescriptionCollectorTest {
    @Test
	public void testCollect() throws CollectionException {
		ScanningActionDescriptionCollector collector = new ScanningActionDescriptionCollector();
		collector.setBasePackages(new String[]{getClass().getPackage().getName() + ".test"});
		collector.setAnnotationClass(Secured.class);
		List<ActionDescription> descriptions = collector.collect();
		assertNotNull("Null result", descriptions);
        assertEquals("Wrong number of actions collected", 6, descriptions.size());

		Set<String> names = new HashSet<String>(descriptions.size());
		for (ActionDescription d : descriptions) {
            assertFalse("Duplicate name " + d.getName(), names.contains(d.getName()));
			names.add(d.getName());
		}
		
        assertTrue(names.contains("sub"));
        assertTrue(names.contains("single"));
        assertTrue(names.contains("multiple1"));
        assertTrue(names.contains("multiple2"));
        assertTrue(names.contains("nested"));
        assertTrue(names.contains("method"));
        assertFalse(names.contains("action_temp_password"));
	}

    @Test
	public void testCollectSameParametersMethods() throws CollectionException {
		ScanningActionDescriptionCollector collector = new ScanningActionDescriptionCollector();
		collector.setBasePackages(new String[]{getClass().getPackage().getName() + ".test_methods"});
		collector.setAnnotationClass(Secured.class);
		List<ActionDescription> descriptions = collector.collect();
		assertNotNull("Null result", descriptions);
		assertEquals("Wrong number of actions collected", 4, descriptions.size());

		Set<String> names = new HashSet<String>(descriptions.size());
		for (ActionDescription d : descriptions) {
			assertFalse("Duplicate name " + d.getName(), names.contains(d.getName()));
			names.add(d.getName());
		}
		
		assertTrue(names.contains("method1"));
		assertTrue(names.contains("method2"));
		assertTrue(names.contains("multiple_method1"));
		assertTrue(names.contains("multiple_method2"));
	}
}
