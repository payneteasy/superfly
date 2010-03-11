package com.payneteasy.superfly.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

/**
 * ActionDescriptionCollector implementation which just takes a comma-delimited
 * list of action names and breaks it to fragments.
 * 
 * @author Roman Puchkovskiy
 */
public class CommaDelimitedListActionDescriptionCollector implements
		ActionDescriptionCollector {
	
	private String commaDelimitedList;

	@Required
	public void setCommaDelimitedList(String commaDelimitedList) {
		this.commaDelimitedList = commaDelimitedList;
	}

	public List<ActionDescription> collect() throws CollectionException {
		String[] actionNames = StringUtils.commaDelimitedListToStringArray(commaDelimitedList);
		List<ActionDescription> result = new ArrayList<ActionDescription>(actionNames.length);
		for (String actionName : actionNames) {
			result.add(new ActionDescription(actionName));
		}
		return result;
	}

}
