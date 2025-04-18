package com.payneteasy.superfly.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XmlActionDescriptionCollectorTest {
    @Test
    public void testCollect() throws CollectionException {
        ActionDescriptionRoot newRoot = new ActionDescriptionRoot(List.of(
                new ActionDescriptionBean("action3", "description3"),
                new ActionDescriptionBean("action4", "description4")
        ));
        XmlMapper xmlMapper = new XmlMapper();
        try {
            String newXml = xmlMapper.writeValueAsString(newRoot);
            System.out.println(newXml);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


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
