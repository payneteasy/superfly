package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ActionsXmlParserTest {

    private final ActionsXmlParser parser = new ActionsXmlParser();

    @Test
    public void parseReadsActionElementsWithAttributes() throws Exception {
        String xml = "<?xml version=\"1.0\"?><actions>"
                + "<action name=\"a1\" description=\"d1\"/>"
                + "<action name=\"a2\" description=\"d2\"/>"
                + "</actions>";
        List<ActionDescription> actions = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertNotNull(actions);
        assertEquals(2, actions.size());
        assertEquals("a1", actions.get(0).getName());
        assertEquals("d1", actions.get(0).getDescription());
        assertEquals("a2", actions.get(1).getName());
        assertEquals("d2", actions.get(1).getDescription());
    }

    @Test
    public void parseReturnsEmptyListWhenNoActions() throws Exception {
        String xml = "<?xml version=\"1.0\"?><actions></actions>";
        List<ActionDescription> actions = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        assertNotNull(actions);
        assertEquals(0, actions.size());
    }
}
