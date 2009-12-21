package org.payneteasy.superfly.client;

import java.util.List;

import org.payneteasy.superfly.client.exception.CollectionException;

import com.payneteasy.superfly.api.ActionDescription;

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
