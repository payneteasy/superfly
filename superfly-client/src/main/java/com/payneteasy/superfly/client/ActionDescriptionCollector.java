package com.payneteasy.superfly.client;

import java.util.List;


import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.exception.CollectionException;

/**
 * Collects and returns action descriptions.
 * 
 * @author Roman Puchkovskiy
 */
public interface ActionDescriptionCollector {
	/**
	 * Returns action descriptions.
	 * 
	 * @return action descriptions
	 * @throws CollectionException 
	 */
	List<ActionDescription> collect() throws CollectionException;
}
