package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ActionDescriptionCollector implementation which returns a list of actions
 * defined in an XML file.
 *
 * @author Roman Puchkovskiy
 */
public class XmlActionDescriptionCollector implements ActionDescriptionCollector {

    private static final Logger logger = LoggerFactory.getLogger(XmlActionDescriptionCollector.class);

    private final ActionsXmlParser parser = new ActionsXmlParser();
    private File file;

    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Sets resource path; the collector will use getFile() from a Resource (adapter layer).
     * This setter is for compatibility when used from Spring — inject a File or use setFile.
     */
    public void setResource(Object resource) {
        if (resource instanceof File) {
            this.file = (File) resource;
        } else if (resource != null && resource.getClass().getName().startsWith("org.springframework")) {
            try {
                this.file = (File) resource.getClass().getMethod("getFile").invoke(resource);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot get File from resource: " + resource, e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported resource type: " + (resource == null ? "null" : resource.getClass().getName()));
        }
    }

    @Override
    public List<ActionDescription> collect() {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("XML file not set or does not exist");
        }
        try {
            List<ActionDescription> actions = parser.parse(file);
            if (logger.isDebugEnabled()) {
                logger.debug("Collected the following actions: {}", actions);
            }
            return actions;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
