package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.junit.Test;
import org.springframework.security.access.annotation.Secured;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

        Set<String> names = buildNamesSetCheckingForDuplications(descriptions);

        Set<String> expectedNames = new HashSet<String>() {{
            add("sub");
            add("single");
            add("multiple1");
            add("multiple2");
            add("nested");
            add("method");
        }};

        assertEquals(new TreeSet<>(expectedNames), new TreeSet<>(names));
    }

    @Test
    public void testCollectSameParametersMethods() throws CollectionException {
        ScanningActionDescriptionCollector collector = new ScanningActionDescriptionCollector();
        collector.setBasePackages(new String[]{getClass().getPackage().getName() + ".test_methods"});
        collector.setAnnotationClass(Secured.class);
        List<ActionDescription> descriptions = collector.collect();
        assertNotNull("Null result", descriptions);
        assertEquals("Wrong number of actions collected", 4, descriptions.size());

        Set<String> names = buildNamesSetCheckingForDuplications(descriptions);

        Set<String> expectedNames = new HashSet<String>() {{
            add("method1");
            add("method2");
            add("multiple_method1");
            add("multiple_method2");
        }};

        assertEquals(new TreeSet<>(expectedNames), new TreeSet<>(names));
    }

    private Set<String> buildNamesSetCheckingForDuplications(List<ActionDescription> descriptions) {
        Set<String> names = new HashSet<>(descriptions.size());
        for (ActionDescription d : descriptions) {
            assertFalse("Duplicate name " + d.getName(), names.contains(d.getName()));
            names.add(d.getName());
        }
        return names;
    }
}
