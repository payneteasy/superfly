package com.payneteasy.superfly.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

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
		List<ActionDescriptionBean> list = new ArrayList<ActionDescriptionBean>();
		Digester digester = new Digester();
		digester.push(list);
		digester.setNamespaceAware(false);
		digester.setValidating(false);

		digester.addObjectCreate("actions/action", ActionDescriptionBean.class);
		digester.addSetProperties("actions/action");
		digester.addSetNext("actions/action", "add", ActionDescriptionBean.class.getName());
		
		try {
			digester.parse(resource.getInputStream());
		} catch (IOException e) {
			throw new CollectionException(e);
		} catch (SAXException e) {
			throw new CollectionException(e);
		}
		
		List<ActionDescription> actions = new ArrayList<ActionDescription>(list.size());
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
