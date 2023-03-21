package com.payneteasy.superfly.client;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ActionDescriptionCollector implementation which returns a list of actions
 * defined in an XML file.
 * 
 * @author Roman Puchkovskiy
 */
public class XmlActionDescriptionCollector implements ActionDescriptionCollector {

    private static final Logger logger = LoggerFactory.getLogger(XmlActionDescriptionCollector.class);

    private Resource resource;

    @Required
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<ActionDescription> collect() throws CollectionException {
        List<ActionDescriptionBean> list;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ActionDescriptionRoot.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            ActionDescriptionRoot news = (ActionDescriptionRoot) jaxbUnmarshaller.unmarshal(resource.getFile());
            list = new ArrayList<>(news.getActions());
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }

        List<ActionDescription> actions = new ArrayList<>(list.size());
        for (ActionDescriptionBean bean : list) {
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
