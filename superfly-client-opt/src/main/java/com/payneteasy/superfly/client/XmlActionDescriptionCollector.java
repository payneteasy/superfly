package com.payneteasy.superfly.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.payneteasy.superfly.api.ActionDescription;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;

import javax.xml.XMLConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ActionDescriptionCollector implementation which returns a list of actions
 * defined in an XML file.
 *
 * @author Roman Puchkovskiy
 */
@Setter
public class XmlActionDescriptionCollector implements ActionDescriptionCollector {

    private static final Logger logger = LoggerFactory.getLogger(XmlActionDescriptionCollector.class);

    private Resource resource;

    public List<ActionDescription> collect() {
        List<ActionDescription> list;
        try {
            // Создаем XmlMapper вместо JAXBContext
            File                  xmlFile   = resource.getFile();
            XmlMapper             xmlMapper = new XmlMapper();

            // fix for vulnerability CVE-2022-40152
            xmlMapper.getFactory().getXMLInputFactory().setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            ActionDescriptionRoot root      = xmlMapper.readValue(xmlFile, ActionDescriptionRoot.class);
            list = new ArrayList<>(root.getActions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<ActionDescription> actions = new ArrayList<>(list.size());
        for (ActionDescription bean : list) {
            ActionDescription action = new ActionDescription();
            BeanUtils.copyProperties(bean, action);
            actions.add(action);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Collected the following actions: " + actions);
        }

        return actions;
    }

}
