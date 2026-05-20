package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses actions XML format using StAX (no external XML library required).
 * Expected format: {@code <actions><action name="..." description="..."/>...</actions>}
 */
public final class ActionsXmlParser {

    private static final String ELEMENT_ACTIONS = "actions";
    private static final String ELEMENT_ACTION = "action";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DESCRIPTION = "description";

    private final XMLInputFactory inputFactory;

    public ActionsXmlParser() {
        this.inputFactory = createSecureInputFactory();
    }

    private static XMLInputFactory createSecureInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            factory.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (Exception ignored) {
            // property may not be supported by all implementations
        }
        return factory;
    }

    /**
     * Parses XML from the given file and returns a list of action descriptions.
     *
     * @param file XML file with root element {@code <actions>}
     * @return list of actions (never null)
     * @throws IOException if reading or parsing fails
     */
    public List<ActionDescription> parse(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return parse(in);
        }
    }

    /**
     * Parses XML from the given input stream. Caller is responsible for closing the stream.
     *
     * @param inputStream XML input with root element {@code <actions>}
     * @return list of actions (never null)
     * @throws IOException if parsing fails
     */
    public List<ActionDescription> parse(InputStream inputStream) throws IOException {
        try {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(inputStream);
            try {
                return parse(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException e) {
            throw new IOException("Failed to parse actions XML", e);
        }
    }

    private List<ActionDescription> parse(XMLStreamReader reader) throws XMLStreamException {
        List<ActionDescription> actions = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                if (ELEMENT_ACTION.equals(localName)) {
                    String name = getAttribute(reader, ATTR_NAME);
                    String description = getAttribute(reader, ATTR_DESCRIPTION);
                    actions.add(new ActionDescriptionBean(name != null ? name : "", description));
                }
            }
        }

        return actions;
    }

    private static String getAttribute(XMLStreamReader reader, String localName) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (localName.equals(reader.getAttributeLocalName(i))) {
                return reader.getAttributeValue(i);
            }
        }
        return null;
    }
}
