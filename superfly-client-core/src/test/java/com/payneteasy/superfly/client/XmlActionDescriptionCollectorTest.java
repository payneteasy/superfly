package com.payneteasy.superfly.client;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class XmlActionDescriptionCollectorTest {

    @Test(expected = IllegalArgumentException.class)
    public void setResourceThrowsForUnsupportedType() {
        XmlActionDescriptionCollector collector = new XmlActionDescriptionCollector();
        collector.setResource("not-a-file-or-spring-resource");
    }

    @Test
    public void collectThrowsWhenFileNotSet() {
        XmlActionDescriptionCollector collector = new XmlActionDescriptionCollector();
        try {
            collector.collect();
            fail("Expected IllegalStateException when file is not set");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void setResourceAcceptsFile() {
        XmlActionDescriptionCollector collector = new XmlActionDescriptionCollector();
        collector.setResource(new File("actions.xml"));
        // setResource itself doesn't validate existence — collect() does
    }
}
