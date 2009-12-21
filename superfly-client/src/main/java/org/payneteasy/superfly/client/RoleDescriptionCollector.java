package org.payneteasy.superfly.client;

import java.util.List;

import org.payneteasy.superfly.client.exception.CollectionException;

import com.payneteasy.superfly.api.RoleDescription;

/**
 * Collects and returns role descriptions.
 * 
 * @author Roman Puchkovskiy
 */
public interface RoleDescriptionCollector {
	/**
	 * Returns role descriptions.
	 * 
	 * @return role descriptions
	 * @throws CollectionException 
	 */
	List<RoleDescription> collect() throws CollectionException;
}
